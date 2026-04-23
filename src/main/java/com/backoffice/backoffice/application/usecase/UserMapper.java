package com.backoffice.backoffice.application.usecase;

import com.backoffice.backoffice.domain.entity.UserEntity;
import com.backoffice.backoffice.domain.model.User;
import com.backoffice.backoffice.domain.model.UserStatus;

import java.util.UUID;

public class UserMapper {

    public static User toDomain(UserEntity e) {
        if (e == null) return null;

        User u = new User();

        u.id = e.id;
        u.username = e.username;
        u.email = e.email;

        u.firstName = e.firstName;
        u.lastName = e.lastName;

        // 🔥 FIX: calcular en vez de depender del entity
        u.fullName = buildFullName(e.firstName, e.lastName);

        u.phone = e.phone;
        u.avatarUrl = e.avatarUrl;
        u.roleId = e.roleId;

        u.status = e.status != null
                ? UserStatus.valueOf(e.status)
                : UserStatus.ACTIVE;

        return u;
    }

    public static UserEntity toEntity(User u) {
        if (u == null) return null;

        UserEntity e = new UserEntity();

        e.id = u.id != null ? u.id : UUID.randomUUID();
        e.username = u.username;
        e.email = u.email;

        e.firstName = u.firstName;
        e.lastName = u.lastName;

        // 🔥 FIX: siempre recalcular
        e.fullName = buildFullName(u.firstName, u.lastName);

        e.phone = u.phone;
        e.avatarUrl = u.avatarUrl;
        e.roleId = u.roleId;

        e.status = u.status != null ? u.status.name() : "ACTIVE";

        return e;
    }

    private static String buildFullName(String first, String last) {
        return ((first != null ? first : "") + " " +
                (last != null ? last : "")).trim();
    }
}