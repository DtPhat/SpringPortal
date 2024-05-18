package com.swd.uniportal.application.admission.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public final class ModifyAdmissionMajorDto {

    String name;
    String description;

    @NotNull(message = "Quota must be defined.")
    @Min(value = 1, message = "Quota must be positive.")
    Integer quota;

    @NotNull(message = "Major must be defined.")
    @Min(value = 1, message = "Major id must be positive.")
    Long majorId;

    @NotNull(message = "Admission training program must be defined.")
    @Min(value = 1, message = "Admission training program id must be positive.")
    Long admissionTrainingProgramId;
}
