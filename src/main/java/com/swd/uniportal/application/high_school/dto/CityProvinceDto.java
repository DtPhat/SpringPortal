package com.swd.uniportal.application.high_school.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public final class CityProvinceDto {

    private Long id;
    private String name;
}
