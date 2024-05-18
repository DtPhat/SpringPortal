package com.swd.uniportal.application.admission.mapper;

import com.swd.uniportal.application.admission.dto.AdmissionMajorDto;
import com.swd.uniportal.application.admission.dto.AdmissionMajorMethodDto;
import com.swd.uniportal.application.admission.dto.AdmissionMethodDto;
import com.swd.uniportal.application.admission.dto.AdmissionPlanDto;
import com.swd.uniportal.application.admission.dto.AdmissionTrainingProgramDto;
import com.swd.uniportal.application.admission.dto.InstitutionDto;
import com.swd.uniportal.application.admission.dto.MajorDto;
import com.swd.uniportal.application.admission.dto.SubjectGroupDto;
import com.swd.uniportal.application.admission.dto.TrainingProgramDto;
import com.swd.uniportal.application.common.Mapper;
import com.swd.uniportal.domain.admission.AdmissionPlan;
import java.util.Objects;
import org.springframework.stereotype.Component;

@Component
public final class AdmissionPlanMapper implements Mapper<AdmissionPlan, AdmissionPlanDto> {

    @Override
    public AdmissionPlanDto toDto(AdmissionPlan domain) {
        if (Objects.isNull(domain)) {
            return null;
        }
        return AdmissionPlanDto.builder()
                .id(domain.getId())
                .name(domain.getName())
                .description(domain.getDescription())
                .year(domain.getYear())
                .institution(InstitutionDto.builder()
                        .id(domain.getInstitution().getId())
                        .name(domain.getInstitution().getName())
                        .code(domain.getInstitution().getCode())
                        .avatarLink(domain.getInstitution().getAvatarLink())
                        .build())
                .admissionMajors(domain.getAdmissionMajors().stream()
                        .map(am -> AdmissionMajorDto.builder()
                                .id(am.getId())
                                .name(am.getName())
                                .description(am.getDescription())
                                .quota(am.getQuota())
                                .major(Objects.isNull(am.getMajor())
                                        ? null
                                        : MajorDto.builder()
                                                .id(am.getMajor().getId())
                                                .name(am.getMajor().getName())
                                                .code(am.getMajor().getCode())
                                                .build())
                                .admissionTrainingProgram(AdmissionTrainingProgramDto.builder()
                                        .id(am.getAdmissionTrainingProgram().getId())
                                        .name(am.getAdmissionTrainingProgram().getName())
                                        .trainingProgram(TrainingProgramDto.builder()
                                                .id(am.getAdmissionTrainingProgram().getTrainingProgram().getId())
                                                .build())
                                        .build())
                                .admissionMajorMethods(am.getAdmissionMajorMethods().stream()
                                        .map(amm -> AdmissionMajorMethodDto.builder()
                                                .id(amm.getId())
                                                .name(amm.getName())
                                                .admissionMethod(AdmissionMethodDto.builder()
                                                        .id(amm.getAdmissionMethod().getId())
                                                        .name(amm.getAdmissionMethod().getName())
                                                        .code(amm.getAdmissionMethod().getCode())
                                                        .build())
                                                .subjectGroups(amm.getSubjectGroups().stream()
                                                        .map(sgc -> SubjectGroupDto.builder()
                                                                .id(sgc.getId())
                                                                .code(sgc.getCode())
                                                                .build())
                                                        .toList())
                                                .build())
                                        .toList())
                                .build())
                        .toList())
                .admissionTrainingPrograms(domain.getAdmissionTrainingPrograms().stream()
                        .map(atp -> AdmissionTrainingProgramDto.builder()
                                .id(atp.getId())
                                .name(atp.getName())
                                .trainingProgram(TrainingProgramDto.builder()
                                        .id(atp.getTrainingProgram().getId())
                                        .name(atp.getTrainingProgram().getName())
                                        .build())
                                .build())
                        .toList())
                .build();
    }

    @Override
    public AdmissionPlan toDomain(AdmissionPlanDto dto) {
        return null;
    }
}
