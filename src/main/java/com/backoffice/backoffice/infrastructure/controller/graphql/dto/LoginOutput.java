package com.backoffice.backoffice.infrastructure.controller.graphql.dto;

import org.eclipse.microprofile.graphql.Type;

@Type("LoginOutput")
public class LoginOutput {
    public String token;

    public LoginOutput(String token) {
        this.token = token;
    }
}