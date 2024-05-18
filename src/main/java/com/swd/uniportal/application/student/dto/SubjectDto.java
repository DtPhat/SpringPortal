package com.swd.uniportal.application.student.dto;

import java.io.Serializable;
import lombok.Builder;
import lombok.Data;

/**
 * DTO for {@link com.swd.uniportal.domain.subject.Subject}
 */
@Data
@Builder
public class SubjectDto implements Serializable {

    Long id;
    String name;
    String description;
}