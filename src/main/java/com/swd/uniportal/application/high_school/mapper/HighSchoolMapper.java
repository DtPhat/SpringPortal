package com.swd.uniportal.application.high_school.mapper;

import com.swd.uniportal.application.common.Mapper;
import com.swd.uniportal.application.high_school.dto.CityProvinceDto;
import com.swd.uniportal.application.high_school.dto.HighSchoolDto;
import com.swd.uniportal.domain.institution.HighSchool;
import org.springframework.stereotype.Component;

@Component
public final class HighSchoolMapper implements Mapper<HighSchool, HighSchoolDto> {

    @Override
    public HighSchoolDto toDto(HighSchool domain) {
        return HighSchoolDto.builder()
                .id(domain.getId())
                .name(domain.getName())
                .description(domain.getDescription())
                .cityProvince(CityProvinceDto.builder()
                        .id(domain.getCityProvince().getId())
                        .name(domain.getCityProvince().getName())
                        .build())
                .build();
    }

    @Override
    public HighSchool toDomain(HighSchoolDto dto) {
        return null;
    }
}
