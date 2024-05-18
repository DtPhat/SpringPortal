package com.swd.uniportal.application.high_school.dto;

import java.util.List;
import lombok.Builder;

@Builder
public record HighSchoolsDto(Long page, Long totalPages, Long pageSize, Long currentPageSize,
                             List<HighSchoolDto> highSchools) {

}
