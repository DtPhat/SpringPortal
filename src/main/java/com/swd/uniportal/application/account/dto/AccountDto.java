package com.swd.uniportal.application.account.dto;

import lombok.Builder;

@Builder
public record AccountDto(Long id, String email, String firstName, String lastName, String role, String status,
                         String avatarLink) {
}
