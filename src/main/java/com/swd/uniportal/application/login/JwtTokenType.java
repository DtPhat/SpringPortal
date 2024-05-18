package com.swd.uniportal.application.login;

import org.apache.commons.lang3.StringUtils;

public enum JwtTokenType {

    BEARER,
    OTHER;

    public String capitalize() {
        return StringUtils.capitalize(this.name().toLowerCase());
    }
}
