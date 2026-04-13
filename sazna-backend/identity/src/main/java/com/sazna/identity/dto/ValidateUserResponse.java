package com.sazna.identity.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ValidateUserResponse {

    private boolean valid;
    private Long userId;
    private String email;

    public ValidateUserResponse() {}

    public ValidateUserResponse(boolean valid, Long userId, String email) {
        this.valid = valid;
        this.userId = userId;
        this.email = email;
    }

}