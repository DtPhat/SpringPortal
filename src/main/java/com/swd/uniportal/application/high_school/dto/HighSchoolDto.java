package com.swd.uniportal.application.high_school.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public final class HighSchoolDto {

    Long id;
    String name;
    String description;
    CityProvinceDto cityProvince;
}
