package com.swd.uniportal.application.admission.dto;

import com.swd.uniportal.domain.admission.AdmissionMethod;
import java.io.Serializable;
import lombok.Builder;
import lombok.Data;

/**
 * DTO for {@link AdmissionMethod}
 */
@Data
@Builder
public class AdmissionMethodDto implements Serializable {

    Long id;
    String name;
    String code;
    private String description;

}