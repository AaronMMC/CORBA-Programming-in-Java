package com.cs9322.team05.client.player.callback;

import ModifiedHangman.*;
import com.cs9322.team05.client.player.controller.GameController;
import javafx.application.Platform;
import org.omg.CORBA.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.omg.PortableServer.POAManagerPackage.AdapterInactive;
import org.omg.PortableServer.POAPackage.ServantNotActive;
import org.omg.PortableServer.POAPackage.WrongPolicy;

public class ClientCallbackImpl extends ClientCallbackPOA {
    private final ORB orb;
    private final GameService gameService;
    private final String token;
    private GameController controller;

    public ClientCallbackImpl(ORB orb, GameService gameService, String token, GameController controller) {
        this.orb = orb;
        this.gameService = gameService;
        this.token = token;
        this.controller = controller;
    }

    public void register() throws PlayerNotLoggedInException, ServantNotActive, WrongPolicy {
        POA rootPoa = null;
        try {
            rootPoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
        } catch (InvalidName e) {
            throw new RuntimeException(e);
        }
        try {
            rootPoa.the_POAManager().activate();
        } catch (AdapterInactive e) {
            throw new RuntimeException(e);
        }

        org.omg.CORBA.Object ref = rootPoa.servant_to_reference(this);
        ClientCallback stub = ClientCallbackHelper.narrow(ref);

        gameService.registerCallback(stub, token);
    }


    public void setController(GameController controller) {
        this.controller = controller;
    }

    @Override
    public void startRound(int wordLength) {
        Platform.runLater(() -> controller.onStartRound(wordLength));
    }

    @Override
    public void proceedToNextRound(int wordLength) {
        Platform.runLater(() -> controller.onProceedToNextRound(wordLength));
    }

    @Override
    public void endRound(RoundResult result) {
        Platform.runLater(() -> controller.onEndRound(result));
    }

    @Override
    public void endGame(GameResult result) {
        Platform.runLater(() -> controller.onEndGame(result));
    }

    @Override
    public void startGameFailed() {
        Platform.runLater(() -> controller.onEndGame(new GameResult(controller.getGameId(), "Error", /* empty leaderboard */ null)));
    }
}
