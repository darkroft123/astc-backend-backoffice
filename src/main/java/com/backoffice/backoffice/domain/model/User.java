package com.backoffice.backoffice.domain.model;

import java.util.UUID;

public class User {

    public UUID id;

    public String username;
    public String email;

    public String password; // solo uso temporal (CREATE/LOGIN)

    public String firstName;
    public String lastName;

    // 🔥 ESTE ES EL QUE TE FALTA
    public String fullName;

    public String phone;
    public String avatarUrl;

    public UUID roleId;

    public UserStatus status;
}