package com.backoffice.backoffice.infrastructure.controller.graphql.dto;

import com.backoffice.backoffice.domain.model.User;
import org.eclipse.microprofile.graphql.Type;

@Type("UserOutput")
public class UserOutput {

    public String id;
    public String username;
    public String firstName;
    public String lastName;
    public String fullName;
    public String email;
    public String phone;
    public String roleId;
    public String status;

    public static UserOutput from(User u) {
        UserOutput o = new UserOutput();

        o.id = u.id != null ? u.id.toString() : null;
        o.username = u.username;
        o.firstName = u.firstName;
        o.lastName = u.lastName;

        // ⚠️ fullName NO viene del domain model
        o.fullName = (u.firstName != null ? u.firstName : "") + " " +
                     (u.lastName != null ? u.lastName : "");

        o.email = u.email;
        o.phone = u.phone;
        o.roleId = u.roleId != null ? u.roleId.toString() : null;
        o.status = u.status != null ? u.status.name() : null;

        return o;
    }
}