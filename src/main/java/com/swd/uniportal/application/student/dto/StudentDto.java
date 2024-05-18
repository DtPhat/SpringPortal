package com.swd.uniportal.application.student.dto;

import java.time.LocalDate;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StudentDto {

    private Long id;
    private LocalDate birthDate;
    private String phone;
    private HighSchoolDto highSchool;
}
