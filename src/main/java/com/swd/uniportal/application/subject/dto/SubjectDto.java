package com.swd.uniportal.application.subject.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public final class SubjectDto {

    private Long id;
    private String name;
    private String description;
}
