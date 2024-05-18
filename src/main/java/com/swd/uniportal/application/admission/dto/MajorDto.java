package com.swd.uniportal.application.admission.dto;

import java.io.Serializable;
import lombok.Builder;
import lombok.Data;

/**
 * DTO for {@link com.swd.uniportal.domain.major.Major}
 */
@Data
@Builder
public class MajorDto implements Serializable {

    Long id;
    String name;
    String code;
}