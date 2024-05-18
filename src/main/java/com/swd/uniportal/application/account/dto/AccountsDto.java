package com.swd.uniportal.application.account.dto;

import java.util.List;
import lombok.Builder;

@Builder
public record AccountsDto(Long page, Long totalPages, Long pageSize, Long size,
                          List<AccountDto> accounts) {

}
