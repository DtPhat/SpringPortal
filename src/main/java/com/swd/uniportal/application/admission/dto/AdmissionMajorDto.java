package com.swd.uniportal.application.admission.dto;

import com.swd.uniportal.domain.admission.AdmissionMajor;
import java.io.Serializable;
import java.util.List;
import lombok.Builder;
import lombok.Data;

/**
 * DTO for {@link AdmissionMajor}
 */
@Data
@Builder
public class AdmissionMajorDto implements Serializable {

    Long id;
    String name;
    String description;
    Integer quota;
    private List<AdmissionMajorMethodDto> admissionMajorMethods;
    MajorDto major;
    AdmissionTrainingProgramDto admissionTrainingProgram;
}