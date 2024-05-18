package com.swd.uniportal.application.student.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public final class UpdatedStudentRecordDto {

    Long id;

    @NotNull(message = "subjectId: must not be null or blank.")
    private Long subjectId;

    @NotNull(message = "mark: must not be null or blank.")
    private Float mark;

}
