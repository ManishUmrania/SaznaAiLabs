package com.sazna.shared.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ValidateUserRequest {

    private String email;
    private String password;

    public ValidateUserRequest() {}

}