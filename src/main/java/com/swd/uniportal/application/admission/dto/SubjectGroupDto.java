package com.swd.uniportal.application.admission.dto;

import java.io.Serializable;
import lombok.Builder;
import lombok.Data;

/**
 * DTO for {@link com.swd.uniportal.domain.subject.SubjectGroup}
 */
@Data
@Builder
public class SubjectGroupDto implements Serializable {

    Long id;
    String code;
}