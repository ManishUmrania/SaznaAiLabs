package com.sazna.shared.dto;

public class LoginResponse {
    private boolean success;
    private String message;
    private String token;

    public LoginResponse(boolean b, String loginSuccessful, String token) {
        	this.success = b;
        	this.message = loginSuccessful;
        	this.token = token;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}