package com.swd.uniportal.application.admission.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public final class ModifyAdmissionPlanDto {

    @NotBlank(message = "Admission plan name must not be blank.")
    private String name;

    private String description;

    @NotNull(message = "Admission plan year must be defined.")
    @Min(value = 2000, message = "Admission plan year must be in or after 2000.")
    @Max(value = 9999, message = "Admission plan year must be in or before 9999.")
    private Integer year;

    @NotNull(message = "Admission plan institution must be defined.")
    @Min(value = 1, message = "Admission plan institution id must be positive.")
    private Long institutionId;
}
