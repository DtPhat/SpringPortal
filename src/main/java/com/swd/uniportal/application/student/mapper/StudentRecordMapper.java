package com.swd.uniportal.application.student.mapper;

import com.swd.uniportal.application.common.Mapper;
import com.swd.uniportal.application.student.dto.StudentRecordDto;
import com.swd.uniportal.application.student.dto.SubjectDto;
import com.swd.uniportal.domain.student.StudentRecord;
import org.springframework.stereotype.Component;

@Component
public final class StudentRecordMapper implements Mapper<StudentRecord, StudentRecordDto> {

    @Override
    public StudentRecordDto toDto(StudentRecord domain) {
        return StudentRecordDto.builder()
                .id(domain.getId())
                .studentId(domain.getStudent().getId())
                .subject(SubjectDto.builder()
                        .id(domain.getSubject().getId())
                        .name(domain.getSubject().getName())
                        .description(domain.getSubject().getDescription())
                        .build())
                .mark(domain.getMark())
                .build();
    }

    @Override
    public StudentRecord toDomain(StudentRecordDto dto) {
        return null;
    }
}
