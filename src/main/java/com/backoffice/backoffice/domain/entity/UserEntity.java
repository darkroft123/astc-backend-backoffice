package com.backoffice.backoffice.domain.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    public UUID id;

    public String username;
    public String email;

    @Column(name = "password_hash", nullable = false)
    public String passwordHash;

    public String firstName;
    public String lastName;

    @Column(name = "full_name")
    public String fullName;

    public String phone;

    public String avatarUrl;

    @Column(name = "role_id")
    public UUID roleId;

    public String status;

    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;
    public LocalDateTime deletedAt;
}