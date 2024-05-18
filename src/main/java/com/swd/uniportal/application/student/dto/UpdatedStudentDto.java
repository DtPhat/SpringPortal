package com.swd.uniportal.application.student.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public final class UpdatedStudentDto {

    @NotNull(message = "Birth date must be defined.")
    @Past(message = "Birth date must be in the past.")
    private LocalDate birthDate;

    @Size(min = 9, max = 11, message = "Phone number must be between 9 and 11.")
    private String phone;

    @NotNull(message = "High school id must be defined.")
    @Min(value = 1, message = "High school id must be positive.")
    private Long highSchoolId;
}
