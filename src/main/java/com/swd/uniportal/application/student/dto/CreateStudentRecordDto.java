package com.swd.uniportal.application.student.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateStudentRecordDto {

    @NotNull(message = "Subject id must not be null or blank.")
    @Min(value = 1, message = "Subject id must be positive.")
    private Long subjectId;

    @NotNull(message = "Mark must not be null or blank.")
    private Float mark;
}
