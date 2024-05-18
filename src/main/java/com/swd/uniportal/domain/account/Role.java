package com.swd.uniportal.domain.account;

public enum Role {

    ADMIN,
    STAFF,
    STUDENT,
    ANONYMOUS;

    public String asSecurityRole() {
        return "ROLE_" + this.name();
    }
}
