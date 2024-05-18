package com.swd.uniportal.application.student.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public final class StudentRecordDto {

    private Long id;
    private Long studentId;
    private SubjectDto subject;
    private Float mark;
}
