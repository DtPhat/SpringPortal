package com.swd.uniportal.application.admission.mapper;

import com.swd.uniportal.application.admission.dto.AdmissionMajorDto;
import com.swd.uniportal.application.admission.dto.AdmissionMajorMethodDto;
import com.swd.uniportal.application.admission.dto.AdmissionMethodDto;
import com.swd.uniportal.application.admission.dto.AdmissionTrainingProgramDto;
import com.swd.uniportal.application.admission.dto.MajorDto;
import com.swd.uniportal.application.admission.dto.SubjectGroupDto;
import com.swd.uniportal.application.admission.dto.TrainingProgramDto;
import com.swd.uniportal.application.common.Mapper;
import com.swd.uniportal.domain.admission.AdmissionMajor;
import java.util.Objects;
import org.springframework.stereotype.Component;

@Component
public class AdmissionMajorMapper implements Mapper<AdmissionMajor, AdmissionMajorDto> {

    @Override
    public AdmissionMajorDto toDto(AdmissionMajor domain) {
        AdmissionMajorDto.AdmissionMajorDtoBuilder builder = AdmissionMajorDto.builder()
                .id(domain.getId())
                .name(domain.getName())
                .description(domain.getDescription())
                .quota(domain.getQuota())
                .admissionTrainingProgram(AdmissionTrainingProgramDto.builder()
                        .id(domain.getAdmissionTrainingProgram().getId())
                        .name(domain.getAdmissionTrainingProgram().getName())
                        .trainingProgram(TrainingProgramDto.builder()
                                .id(domain.getAdmissionTrainingProgram().getTrainingProgram().getId())
                                .name(domain.getAdmissionTrainingProgram().getTrainingProgram().getName())
                                .build())
                        .build())
                .admissionMajorMethods(domain.getAdmissionMajorMethods().stream()
                        .map(amm -> AdmissionMajorMethodDto.builder()
                                .id(amm.getId())
                                .name(amm.getName())
                                .subjectGroups(amm.getSubjectGroups().stream()
                                        .map(sg -> SubjectGroupDto.builder()
                                                .id(sg.getId())
                                                .code(sg.getCode())
                                                .build())
                                        .toList())
                                .admissionMethod(AdmissionMethodDto.builder()
                                        .id(amm.getAdmissionMethod().getId())
                                        .name(amm.getAdmissionMethod().getName())
                                        .description(amm.getAdmissionMethod().getDescription())
                                        .code(amm.getAdmissionMethod().getCode())
                                        .build())
                                .build())
                        .toList());
        if (Objects.nonNull(domain.getMajor())) {
            builder.major(MajorDto.builder()
                    .id(domain.getMajor().getId())
                    .code(domain.getMajor().getCode())
                    .name(domain.getMajor().getName())
                    .build());
        }
        return builder.build();
    }

    @Override
    public AdmissionMajor toDomain(AdmissionMajorDto dto) {
        return null;
    }
}
