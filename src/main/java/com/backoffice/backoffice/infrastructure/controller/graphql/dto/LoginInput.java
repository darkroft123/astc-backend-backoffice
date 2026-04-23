package com.backoffice.backoffice.infrastructure.controller.graphql.dto;

import org.eclipse.microprofile.graphql.Input;

@Input("LoginInput")
public class LoginInput {
    public String email;
    public String password;
}