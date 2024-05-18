package com.swd.uniportal.application.major.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public final class MajorDto {

    Long id;
    String name;
    String code;
    String description;
    DepartmentDto department;
    SchoolDto school;
}
