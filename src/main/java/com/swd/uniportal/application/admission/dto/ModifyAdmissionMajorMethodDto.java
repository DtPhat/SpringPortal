package com.swd.uniportal.application.admission.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public final class ModifyAdmissionMajorMethodDto {

    @NotBlank(message = "Admission major method must not be null or blank.")
    private String name;

    @NotNull(message = "Admission method id must be defined.")
    @Min(value = 1, message = "Admission method id must be positive.")
    private Long admissionMethodId;

    private List<Long> subjectGroupIds;
}
