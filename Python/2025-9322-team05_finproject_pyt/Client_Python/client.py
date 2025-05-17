import queue
import sys
import time
import threading
import msvcrt
import keyboard

import omniORB
from omniORB import CORBA
import CosNaming

import ModifiedHangman, ModifiedHangman__POA
from clientcallback import ClientCallback


class CallbackHandler:
    def __init__(self):
        self.round_started_event = threading.Event()
        self.game_failed_event = threading.Event()
        self.round_ended_event = threading.Event()
        self.next_round_event = threading.Event()
        self.game_ended_event = threading.Event()
        self.current_word_length = None
        self.current_round_result = None
        self.game_result = None
        self.current_round_number = 0

    def on_round_started(self, roundNumber, wordLength):
        self.current_word_length = wordLength
        self.current_round_number = roundNumber
        self.round_started_event.set()

    def on_game_failed(self):
        self.game_failed_event.set()

    def on_round_ended(self, roundResult):
        print(f"Round {roundResult.roundNumber} ended. Winner: {roundResult.roundWinner.username}")
        self.current_round_result = roundResult
        self.round_ended_event.set()

    def on_next_round(self, wordLength, roundNumber):
        print(f"Proceeding to next round {roundNumber} with word length {wordLength}.")
        self.current_word_length = wordLength
        self.current_round_number = roundNumber
        self.round_started_event.set()
        self.next_round_event.set()

    def on_game_ended(self, gameResult):
        print(f"Game {gameResult.gameId} ended. Winner: {gameResult.gameWinner}")
        self.game_result = gameResult
        self.game_ended_event.set()

def safe_input(prompt, timeout, round_ended_event):
    q = queue.Queue()

    def wait_for_input():
        try:
            user_input = input(prompt)
            q.put(user_input)
        except Exception:
            q.put(None)

    t = threading.Thread(target=wait_for_input)
    t.daemon = True
    t.start()

    start_time = time.time()
    while time.time() - start_time < timeout:
        if round_ended_event.is_set():
            return None
        try:
            return q.get_nowait()
        except queue.Empty:
            time.sleep(0.1)

    return None

def clear_queue(q):
    while not q.empty():
        try:
            q.get_nowait()
        except queue.Empty:
            break

def orb_event_loop(orb):
    try:
        orb.run()
    except Exception as e:
        print(f"ORB event loop terminated: {e}")


def get_naming_service(orb):
    obj = orb.resolve_initial_references("NameService")
    return obj._narrow(CosNaming.NamingContextExt)


def resolve_service(orb, service_name):
    root_context = get_naming_service(orb)
    obj = root_context.resolve_str(service_name)
    if service_name == "AuthenticationService":
        return obj._narrow(ModifiedHangman.AuthenticationService)
    elif service_name == "GameService":
        return obj._narrow(ModifiedHangman.GameService)
    elif service_name == "AdminService":
        return obj._narrow(ModifiedHangman.AdminService)
    else:
        raise Exception(f"Unknown service: {service_name}")


def create_callback_servant(orb, callback_impl):
    poa = orb.resolve_initial_references("RootPOA")
    poa_manager = poa._get_the_POAManager()
    poa_manager.activate()

    poa.activate_object(callback_impl)
    servant_ref = callback_impl._this()
    return servant_ref


def login(auth_service):
    print("Login")
    username = input("Username: ").strip()
    password = input("Password: ").strip()

    if not username:
        print("Error: Username cannot be empty.")
        return None, None

    if not password:
        print("Error: Password cannot be empty.")
        return None, None

    if not password and not username:
        print("Error: Username and Password cannot be empty.")
        return None, None

    try:
        token = auth_service.login(username, password)
        print(f"Login succeeded for user '{username}'")
        return username, token
    except ModifiedHangman.LogInException as e:
        print(f"Login failed: {e.message}")
        return None, None
    except Exception as e:
        print(f"Unexpected error during login: {e}")
        return None, None



def logout(auth_service, token):
    try:
        auth_service.logout(token)
        print("Logged out successfully.")
    except ModifiedHangman.PlayerNotLoggedInException as e:
        print(f"Logout failed: {e.message}")

def guess_letter_loop(game_service, username, game_id, token, callback_handler, input_queue):
    guessed_letters = set()
    print("_" * callback_handler.current_word_length)

    while callback_handler.round_started_event.is_set() and not callback_handler.round_ended_event.is_set():
        try:
            letter = input_queue.get(timeout=0.2)
        except queue.Empty:
            if not callback_handler.round_started_event.is_set() or callback_handler.round_ended_event.is_set():
                break
            continue

        if len(letter) != 1 or not letter.isalpha():
            print("Please enter a single alphabet letter.")
            continue

        letter = letter.lower()
        if letter in guessed_letters:
            print(f"You already guessed '{letter}'. Try a different letter.")
            continue

        guessed_letters.add(letter)

        try:
            response = game_service.guessLetter(username, game_id, letter, token)
            print(f"\nHidden Word: {response.maskedWord}")
            print(f"Remaining attempts: {response.remainingAttemptsLeft}")
            print("Attempted letters:")
            for att in response.attemptedLetters:
                correctness = "correct" if att.isLetterCorrect else "incorrect"
                print(f" - {att.letter}: {correctness}")

            if response.isWordGuessed:
                print("Congratulations! You guessed the word!")
                break

            if response.remainingAttemptsLeft <= 0:
                print("No attempts left. Round over!")
                break

        except ModifiedHangman.GameNotFoundException as e:
            print(f"Game not found: {e.message}")
            break
        except ModifiedHangman.PlayerNotLoggedInException as e:
            print(f"You must be logged in: {e.message}")
            break
        except Exception as e:
            print(f"Error guessing letter: {e}")
            break


def play_game(game_service, username, game_id, token, callback_handler):
    input_queue = queue.Queue()
    stop_event = threading.Event()

    def read_input():
        while not stop_event.is_set():
            try:
                letter = input().strip()
                if letter:
                    input_queue.put(letter)
            except EOFError:
                break

    input_thread = threading.Thread(target=read_input, daemon=True)
    input_thread.start()

    while not callback_handler.game_ended_event.is_set():
        callback_handler.round_started_event.wait()

        if callback_handler.game_ended_event.is_set():
            break

        callback_handler.round_ended_event.clear()

        print(f"Round {callback_handler.current_round_number} starting! Word length: {callback_handler.current_word_length}")

        guess_letter_loop(game_service, username, game_id, token, callback_handler, input_queue)


        print("\nWaiting for round to end...")
        callback_handler.round_ended_event.wait()
        rr = callback_handler.current_round_result
        if rr:
            winner_name = rr.roundWinner.username if rr.roundWinner else "No winner"
            print("\nRound ended!")
            print(f"Round Number: {rr.roundNumber}")
            print(f"Winner: {winner_name}")
            print(f"Word to guess was: {rr.wordToGuess}")
            print(f"{rr.statusMessage}")
            print("Current Score:")
            for player in rr.currentGameLeaderboard:
                print(f" - {player.username}: {player.wins} wins")

        callback_handler.round_started_event.clear()

        print("\nWaiting for next round or game end")
        while not (callback_handler.round_started_event.is_set() or callback_handler.game_ended_event.is_set()):
            time.sleep(0.1)

        if callback_handler.game_ended_event.is_set():
            break

        callback_handler.next_round_event.clear()

    stop_event.set()
    input_thread.join(timeout=1)

    clear_queue(input_queue)

    gr = callback_handler.game_result
    if gr:
        winner_name = gr.gameWinner if gr.gameWinner else "No winner"
        print("\nGame ended!")
        print(f"Game ID: {gr.gameId}")
        print(f"Winner: {winner_name}")
        print("Final Score:")
        for player in gr.leaderboard:
            print(f" - {player.username}: {player.wins} wins")


def start_game(game_service, username, token, callback_impl, callback_handler):
    try:
        callback_handler.round_started_event.clear()
        callback_handler.game_failed_event.clear()
        callback_handler.round_ended_event.clear()
        callback_handler.next_round_event.clear()
        callback_handler.game_ended_event.clear()

        game_info = game_service.start_game(username, token)
        print(f"Game started request sent. Game ID: {game_info.gameId}, round duration: {game_info.roundLength}s")

        wait_time = getattr(game_info, 'waitTime', 10)
        print(f"Waiting up to {wait_time} seconds for other players to join...")

        start_or_fail_event = threading.Event()

        # Thread that listens to either round start or game failure
        def waiter():
            callback_handler.round_started_event.wait()
            start_or_fail_event.set()
            return

        def fail_listener():
            callback_handler.game_failed_event.wait()
            start_or_fail_event.set()
            return

        threading.Thread(target=waiter, daemon=True).start()
        threading.Thread(target=fail_listener, daemon=True).start()

        if not start_or_fail_event.wait(timeout=wait_time + 5):
            print("Timeout waiting for game to start or failure. Try again later.")
            return None

        if callback_handler.game_failed_event.is_set():
            print("Game could not start because no other players joined in time.")
            return None

        elif callback_handler.round_started_event.is_set():
            play_game(game_service, username, game_info.gameId, token, callback_handler)
            if not callback_handler.game_ended_event.is_set():
                print("Waiting for final game result...")
                callback_handler.game_ended_event.wait()
            return game_info.gameId

    except ModifiedHangman.PlayerNotLoggedInException as e:
        print(f"You must be logged in to start a game: {e.message}")
    except Exception as e:
        print(f"Failed to start game: {e}")
    return None


def get_leaderboard(game_service, token):
    try:
        leaderboard = game_service.get_leaderboard(token)
        print("Leaderboard (Top 5):")
        top_players = leaderboard.players[:5]  # Get only the top 5 players
        for idx, player in enumerate(top_players, start=1):
            print(f"{idx}. {player.username}: {player.wins} wins")
    except ModifiedHangman.PlayerNotLoggedInException as e:
        print(f"You must be logged in to get the leaderboard: {e.message}")
    except Exception as e:
        print(f"Failed to get leaderboard: {e}")

def player_menu(game_service, auth_service, username, token, callback_impl, handler):
    while True:
        print("\nOptions:")
        print(" 1. Start new game")
        print(" 2. Get leaderboard")
        print(" 3. Logout")
        print(" 4. Exit program")
        choice = input("Choose an option: ").strip()

        if choice == '1':
            start_game(game_service, username, token, callback_impl, handler)
        elif choice == '2':
            get_leaderboard(game_service, token)
        elif choice == '3':
            logout(auth_service, token)
            return "logout"
        elif choice == '4':
            logout(auth_service, token)
            print("Exiting client.")
            return "exit"
        else:
            print("Invalid choice, try again.")

def main():
    try:
        orb = CORBA.ORB_init([
            "-ORBInitRef",
            "NameService=corbaloc::localhost:1050/NameService"
        ], CORBA.ORB_ID)
        orb_thread = threading.Thread(target=orb_event_loop, args=(orb,), daemon=True)
        orb_thread.start()

        auth_service = resolve_service(orb, "AuthenticationService")
        game_service = resolve_service(orb, "GameService")

        while True:
            username, token = login(auth_service)
            if not token:
                retry = input("Login failed. Try again? (y/n): ").strip().lower()
                if retry != 'y':
                    print("Exiting client.")
                    break
                continue

            handler = CallbackHandler()
            callback_impl = ClientCallback(handler=handler)
            callback_ref = create_callback_servant(orb, callback_impl)

            try:
                auth_service.registerCallback(callback_ref, token)
            except ModifiedHangman.PlayerNotLoggedInException as e:
                print(f"Could not register callback: {e.message}")
                continue

            try:
                game_service.registerCallback(callback_ref, token)
            except ModifiedHangman.PlayerNotLoggedInException as e:
                print(f"Could not register callback: {e.message}")
                continue

            result = player_menu(game_service, auth_service, username, token, callback_impl, handler)
            if result == "exit":
                break

    except Exception as e:
        print(f"An unexpected error occurred: {e}")
    finally:
        try:
            orb.shutdown(wait=False)
        except:
            pass



if __name__ == "__main__":
    main()
