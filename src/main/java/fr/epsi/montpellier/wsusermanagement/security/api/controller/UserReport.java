package fr.epsi.montpellier.wsusermanagement.security.api.controller;

public class UserReport {
    private String login;
    private int status;
    private String message;

    public UserReport(String login, int status, String message) {
        this.login = login;
        this.status = status;
        this.message = message;
    }

    public String getLogin() {
        return login;
    }
    public void setLogin(String login) {
        this.login = login;
    }

    public int getStatus() {
        return status;
    }
    public void setStatus(int result) {
        this.status = result;
    }

    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
}
