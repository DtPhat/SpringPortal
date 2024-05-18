package com.swd.uniportal.application.student.dto;

import java.io.Serializable;
import lombok.Builder;
import lombok.Data;

/**
 * DTO for {@link com.swd.uniportal.domain.institution.HighSchool}
 */
@Data
@Builder
public class HighSchoolDto implements Serializable {

    Long id;
    String name;
    String description;
}