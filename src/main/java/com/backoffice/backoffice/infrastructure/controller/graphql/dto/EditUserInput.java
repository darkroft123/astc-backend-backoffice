package com.backoffice.backoffice.infrastructure.controller.graphql.dto;

import org.eclipse.microprofile.graphql.Input;

@Input("EditUserInput")
public class EditUserInput {

    public String username;
    public String email;

    public String firstName;
    public String lastName;

    public String phone;
    public String avatarUrl;

    public String roleId;

    public String status; // opcional
}
