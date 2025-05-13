package com.cs9322.team05.client.player.model;

import ModifiedHangman.*;
import org.omg.CORBA.Object;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAPackage.ServantNotActive;
import org.omg.PortableServer.POAPackage.WrongPolicy;

public class GameModel {
    private final GameService gameService;
    private final String username, token;
    private POA poa;

    public GameModel(GameService gameService, String username, String token) {
        this.gameService = gameService;
        this.username    = username;
        this.token       = token;
    }

    /**
     * Activate and register the callback servant, storing the POA for later use.
     */
    public void registerCallback(ClientCallbackPOA callbackServant, POA poa)
            throws PlayerNotLoggedInException, WrongPolicy, ServantNotActive {
        this.poa = poa;  // store for getPoa()
        Object ref = poa.servant_to_reference(callbackServant);
        ClientCallback callback = ClientCallbackHelper.narrow(ref);
        gameService.registerCallback(callback, token);
    }

    public GameInfo startGame() throws PlayerNotLoggedInException {
        return gameService.start_game(username, token);
    }

    public GuessResponse guessLetter(String gameId, char letter)
            throws GameNotFoundException, PlayerNotLoggedInException {
        return gameService.guessLetter(username, gameId, letter, token);
    }

    public Leaderboard getLeaderboard() throws PlayerNotLoggedInException {
        return gameService.get_leaderboard(token);
    }

    public POA getPoa() {
        return poa;
    }
}
