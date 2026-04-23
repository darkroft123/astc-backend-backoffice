package com.backoffice.backoffice.domain.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "roles")
public class RoleEntity {

    @Id
    public UUID id;

    public String code;
    public String name;
}