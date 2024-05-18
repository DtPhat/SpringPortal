package com.swd.uniportal.application.admission.dto;

import com.swd.uniportal.domain.admission.AdmissionMajorMethod;
import java.io.Serializable;
import java.util.List;
import lombok.Builder;
import lombok.Data;

/**
 * DTO for {@link AdmissionMajorMethod}
 */
@Data
@Builder
public class AdmissionMajorMethodDto implements Serializable {

    Long id;
    String name;
    AdmissionMethodDto admissionMethod;
    private List<SubjectGroupDto> subjectGroups;
}