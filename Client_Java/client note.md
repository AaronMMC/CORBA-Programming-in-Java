1. Player A starts a game
2. Client code will invoke the startGame of the GameService
3. Client code will receive a game info contains the gameId, roundTimeLength, and the remaining waiting time (0 - 10 secs default). During this, just display the timer (remaining time counting down to 0).
- it can be the case that this player will host a game (timer will be 10 seconds) or joining a game (timer will be less than 10 seconds but greater than 0 sec. ) if there exist a player who hosted a game not less than 10 seconds ago.
4. After the 0 second, the server will call(callback) the method startGame(int wordLength) in the client side with the wordLength to be guessed.
5. Then the client will start to guess the word. (_ _ _ _ _ _ _ _)
6. If a player guesses, the client code will invoke the guessLetter of the GameService, and will be receiving a GuessResponse object which contains
    - boolean isWordGuessed;
    - string maskedWord;
    - sequence<AttemptedLetter> attemptedLetters; // the AttemptedLetter has 2 attributes, the letter and if it is a correct letter
    - long remainingAttemptsLeft;

7. it will continue to do so until the isWordGuessed became true or the remaining Attempts become 0.
 - if the player guessed it in less than 30 seconds, he will then have to wait for other players to guess it correctly or wait the rest of the 30 seconds (if at least one    player can't guess it).

8. after a round, the server will call 1-2 methods in the ClientCallback. 2 methods if there is no winner yet (3 round wins) and 1 method if there is already a winner. (the "a" "b" are the methods to be called by the server)
    - if there is still no winner. display RoundResult for a few seconds then proceed to the next round.
      a. endRound(RoundResult) - this contains
      string gameId;
      string roundWinner;
      long roundWinnerTime;
      string statusMessage; // it can be "You've done it in 20 seconds" or "You did not guessed it" or "You won this round"
      sequence<GamePlayer> currentGameLeaderboard; // the GamePlayer contains the username and the number of roundwins in the game.
      b. proceedToNextRound(long wordLength) - this is the length of the next letter to be guessed.
    - if there is already a winner (3 round wins). display the game result with a home button.
      a. endGame(GameResult) - this contains
      string gameId;
      string gameWinner;
      sequence<GamePlayer> leaderboard;


