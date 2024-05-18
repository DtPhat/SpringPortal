package com.swd.uniportal.application.admission.mapper;

import com.swd.uniportal.application.admission.dto.AdmissionMajorMethodDto;
import com.swd.uniportal.application.admission.dto.AdmissionMethodDto;
import com.swd.uniportal.application.admission.dto.SubjectGroupDto;
import com.swd.uniportal.application.common.Mapper;
import com.swd.uniportal.domain.admission.AdmissionMajorMethod;
import org.springframework.stereotype.Component;

@Component
public final class AdmissionMajorMethodMapper implements Mapper<AdmissionMajorMethod, AdmissionMajorMethodDto> {

    @Override
    public AdmissionMajorMethodDto toDto(AdmissionMajorMethod domain) {
        return AdmissionMajorMethodDto.builder()
                .id(domain.getId())
                .name(domain.getName())
                .admissionMethod(AdmissionMethodDto.builder()
                        .id(domain.getAdmissionMethod().getId())
                        .name(domain.getAdmissionMethod().getName())
                        .code(domain.getAdmissionMethod().getCode())
                        .build())
                .subjectGroups(domain.getSubjectGroups().stream()
                        .map(sg -> SubjectGroupDto.builder()
                                .id(sg.getId())
                                .code(sg.getCode())
                                .build())
                        .toList())
                .build();
    }

    @Override
    public AdmissionMajorMethod toDomain(AdmissionMajorMethodDto dto) {
        return null;
    }
}
