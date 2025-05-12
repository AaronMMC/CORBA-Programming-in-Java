// src/main/java/com/cs9322/team05/client/player/model/GameModel.java
package com.cs9322.team05.client.player.model;

import ModifiedHangman.*;
import org.omg.CORBA.Object;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAPackage.ServantNotActive;
import org.omg.PortableServer.POAPackage.WrongPolicy;

public class GameModel {
    private final GameService gameService;
    private final String username, token;

    public GameModel(GameService gameService, String username, String token) {
        this.gameService = gameService;
        this.username    = username;
        this.token       = token;
    }

    public void registerCallback(ClientCallbackPOA callbackServant, POA poa)
            throws PlayerNotLoggedInException, WrongPolicy, ServantNotActive {
        // Activate servant in POA
        Object ref = poa.servant_to_reference(callbackServant);
        ClientCallback callback = ClientCallbackHelper.narrow(ref);
        gameService.registerCallback(callback, token);
    }

    /** Host or join a game; returns the timings and gameId. */
    public GameInfo startGame() throws PlayerNotLoggedInException {
        return gameService.start_game(username, token);
    }

    /** Submit one letter guess, get back the new game state. */
    public GuessResponse guessLetter(String gameId, char letter)
            throws GameNotFoundException, PlayerNotLoggedInException {
        return gameService.guessLetter(username, gameId, letter, token);
    }

    /** Retrieve the current full leaderboard at any time. */
    public Leaderboard getLeaderboard() throws PlayerNotLoggedInException {
        return gameService.get_leaderboard(token);
    }
}
