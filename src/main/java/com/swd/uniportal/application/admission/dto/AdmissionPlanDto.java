package com.swd.uniportal.application.admission.dto;

import java.io.Serializable;
import java.util.List;
import lombok.Builder;
import lombok.Data;

/**
 * DTO for {@link com.swd.uniportal.domain.admission.AdmissionPlan}
 */
@Data
@Builder
public class AdmissionPlanDto implements Serializable {

    Long id;
    String name;
    String description;
    Integer year;
    InstitutionDto institution;
    private List<AdmissionMajorDto> admissionMajors;
    private List<AdmissionTrainingProgramDto> admissionTrainingPrograms;
}