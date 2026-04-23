package com.backoffice.backoffice.infrastructure.controller.graphql.dto;

import org.eclipse.microprofile.graphql.Input;

@Input("CreateUserInput")
public class CreateUserInput {

    public String username;
    public String firstName;
    public String lastName;
    public String email;
    public String password; // ✅ obligatorio
    public String phone;
    public String roleId;
}