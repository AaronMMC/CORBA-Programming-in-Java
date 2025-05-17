import threading
from omniORB import PortableServer
import ModifiedHangman__POA

class ClientCallback(ModifiedHangman__POA.ClientCallback):
    def __init__(self, handler=None):
        self.handler = handler

    def startRound(self, wordLength, roundNumber):
        if self.handler:
            self.handler.on_round_started(roundNumber, wordLength)

    def startGameFailed(self):
        if self.handler:
            self.handler.on_game_failed()

    def endGame(self, gameResultData):
        print(f"endGame called: Game ended. Winner: {gameResultData.gameWinner}")
        if self.handler:
            self.handler.on_game_ended(gameResultData)

    def endRound(self, roundResultData):
        print(f"endRound called: Round {roundResultData.roundNumber} ended.")
        if self.handler:
            self.handler.on_round_ended(roundResultData)

    def proceedToNextRound(self, wordLength, roundNumber):
        print(f"proceedToNextRound called: Next round {roundNumber} with word length {wordLength}")
        if self.handler:
            self.handler.on_next_round(wordLength)
