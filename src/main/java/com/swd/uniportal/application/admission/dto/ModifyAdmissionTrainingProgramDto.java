package com.swd.uniportal.application.admission.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public final class ModifyAdmissionTrainingProgramDto {

    @NotBlank(message = "Admission training program name must not be blank.")
    private String name;

    @NotNull(message = "Admission training program must be linked to predefined training program.")
    @Min(value = 1, message = "Training program id must be positive.")
    private Long trainingProgramId;
}
