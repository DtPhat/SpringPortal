package com.swd.uniportal.application.account.dto;

import com.swd.uniportal.domain.account.Role;
import com.swd.uniportal.domain.account.Status;
import lombok.Data;

@Data
public final class UpdateAccountDto {

    private Role role;
    private Status status;
}
