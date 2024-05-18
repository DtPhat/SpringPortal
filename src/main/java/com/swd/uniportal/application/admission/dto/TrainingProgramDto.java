package com.swd.uniportal.application.admission.dto;

import com.swd.uniportal.domain.admission.TrainingProgram;
import java.io.Serializable;
import lombok.Builder;
import lombok.Data;

/**
 * DTO for {@link TrainingProgram}
 */
@Data
@Builder
public class TrainingProgramDto implements Serializable {

    Long id;
    String name;
}