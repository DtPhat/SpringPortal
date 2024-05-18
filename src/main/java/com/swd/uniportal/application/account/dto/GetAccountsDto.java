package com.swd.uniportal.application.account.dto;

import com.swd.uniportal.domain.account.Role;
import com.swd.uniportal.domain.account.Status;
import com.swd.uniportal.infrastructure.common.SortOrder;
import lombok.Builder;

@Builder
public record GetAccountsDto(String search, Role role, Status status, SortOrder sortOrder,
                             Long page) {

}
