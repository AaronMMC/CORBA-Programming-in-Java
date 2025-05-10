package com.cs9322.team05.client.player.interfaces;

public interface ILoginView {
    String getUsername();

    String getPassword();

    void showMessage(String message, boolean isError);

    void clearFields();

    void showLoginSuccess();

    void showLoginError(String errorMessage);

    void setLoginButtonEnabled(boolean enabled);

    void showLoadingIndicator(boolean show);
}
