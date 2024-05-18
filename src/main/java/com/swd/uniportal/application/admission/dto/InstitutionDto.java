package com.swd.uniportal.application.admission.dto;

import java.io.Serializable;
import lombok.Builder;
import lombok.Data;

/**
 * DTO for {@link com.swd.uniportal.domain.institution.Institution}
 */
@Data
@Builder
public class InstitutionDto implements Serializable {

    Long id;
    String name;
    String code;
    String avatarLink;
}