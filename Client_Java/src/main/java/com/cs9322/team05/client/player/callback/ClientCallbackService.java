package com.cs9322.team05.client.player.callback;

import ModifiedHangman.ClientCallback;
import ModifiedHangman.GameResult;
import ModifiedHangman.RoundResult;
import com.cs9322.team05.client.player.controller.GameController;
import org.omg.CORBA.*;
import org.omg.CORBA.Object;

/**
 * Implements only the IDL interface.  No POA, no ORB, no FX in here.
 */
public class ClientCallbackService implements ClientCallback {
    private final GameController controller;

    public ClientCallbackService(GameController controller) {
        this.controller = controller;
    }

    @Override
    public void startRound(int wordLength) {
        controller.onStartRound(wordLength);
    }

    @Override
    public void proceedToNextRound(int wordLength) {
        controller.onProceedToNextRound(wordLength);
    }

    @Override
    public void endRound(RoundResult result) {
        controller.onEndRound(result);
    }

    @Override
    public void endGame(GameResult result) {
        controller.onEndGame(result);
    }

    @Override
    public void startGameFailed() {
        controller.onEndGame(
                new GameResult(controller.getGameId(), "Error", null)
        );
    }

    @Override
    public boolean _is_a(String s) {
        return false;
    }

    @Override
    public boolean _is_equivalent(Object object) {
        return false;
    }

    @Override
    public boolean _non_existent() {
        return false;
    }

    @Override
    public int _hash(int i) {
        return 0;
    }

    @Override
    public Object _duplicate() {
        return null;
    }

    @Override
    public void _release() {

    }

    @Override
    public Object _get_interface_def() {
        return null;
    }

    @Override
    public Request _request(String s) {
        return null;
    }

    @Override
    public Request _create_request(Context context, String s, NVList nvList, NamedValue namedValue) {
        return null;
    }

    @Override
    public Request _create_request(Context context, String s, NVList nvList, NamedValue namedValue, ExceptionList exceptionList, ContextList contextList) {
        return null;
    }

    @Override
    public Policy _get_policy(int i) {
        return null;
    }

    @Override
    public DomainManager[] _get_domain_managers() {
        return new DomainManager[0];
    }

    @Override
    public Object _set_policy_override(Policy[] policies, SetOverrideType setOverrideType) {
        return null;
    }
}
