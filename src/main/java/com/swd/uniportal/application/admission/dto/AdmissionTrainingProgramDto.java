package com.swd.uniportal.application.admission.dto;

import com.swd.uniportal.domain.admission.AdmissionTrainingProgram;
import java.io.Serializable;
import lombok.Builder;
import lombok.Data;

/**
 * DTO for {@link AdmissionTrainingProgram}
 */
@Data
@Builder
public class AdmissionTrainingProgramDto implements Serializable {

    Long id;
    String name;
    TrainingProgramDto trainingProgram;
}