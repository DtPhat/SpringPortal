package com.swd.uniportal.application.student.mapper;

import com.swd.uniportal.application.common.Mapper;
import com.swd.uniportal.application.student.dto.HighSchoolDto;
import com.swd.uniportal.application.student.dto.StudentDto;
import com.swd.uniportal.domain.student.Student;
import org.springframework.stereotype.Component;

@Component
public final class StudentMapper implements Mapper<Student, StudentDto> {

    @Override
    public StudentDto toDto(Student domain) {
        return StudentDto.builder()
                .id(domain.getId())
                .phone(domain.getPhone())
                .birthDate(domain.getBirthDate())
                .highSchool(HighSchoolDto.builder()
                        .id(domain.getHighSchool().getId())
                        .name(domain.getHighSchool().getName())
                        .description(domain.getHighSchool().getDescription())
                        .build())
                .build();
    }

    @Override
    public Student toDomain(StudentDto dto) {
        return null;
    }
}
