package com.swd.uniportal.application.major.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public final class SchoolDto {

    private Long id;
    private String name;
    private String code;
}
