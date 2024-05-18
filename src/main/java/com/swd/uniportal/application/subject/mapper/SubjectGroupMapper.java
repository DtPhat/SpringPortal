package com.swd.uniportal.application.subject.mapper;

import com.swd.uniportal.application.common.Mapper;
import com.swd.uniportal.application.subject.dto.SubjectGroupDto;
import com.swd.uniportal.domain.subject.Subject;
import com.swd.uniportal.domain.subject.SubjectGroup;

public final class SubjectGroupMapper implements Mapper<SubjectGroup, SubjectGroupDto> {

    @Override
    public SubjectGroupDto toDto(SubjectGroup domain) {
        return SubjectGroupDto.builder()
                .id(domain.getId())
                .code(domain.getCode())
                .subjects(domain.getSubjects().stream()
                        .map(Subject::getName)
                        .toList())
                .build();
    }

    @Override
    public SubjectGroup toDomain(SubjectGroupDto dto) {
        return null;
    }
}
