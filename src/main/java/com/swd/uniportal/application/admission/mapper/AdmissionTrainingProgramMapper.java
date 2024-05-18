package com.swd.uniportal.application.admission.mapper;

import com.swd.uniportal.application.admission.dto.AdmissionTrainingProgramDto;
import com.swd.uniportal.application.admission.dto.TrainingProgramDto;
import com.swd.uniportal.application.common.Mapper;
import com.swd.uniportal.domain.admission.AdmissionTrainingProgram;
import com.swd.uniportal.domain.admission.TrainingProgram;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public final class AdmissionTrainingProgramMapper implements Mapper<AdmissionTrainingProgram, AdmissionTrainingProgramDto> {

    private final Mapper<TrainingProgram, TrainingProgramDto> trainingProgramMapper;

    @Autowired
    public AdmissionTrainingProgramMapper(TrainingProgramMapper trainingProgramMapper) {
        this.trainingProgramMapper = trainingProgramMapper;
    }

    @Override
    public AdmissionTrainingProgramDto toDto(AdmissionTrainingProgram domain) {
        return AdmissionTrainingProgramDto.builder()
                .id(domain.getId())
                .name(domain.getName())
                .trainingProgram(trainingProgramMapper.toDto(domain.getTrainingProgram()))
                .build();
    }

    @Override
    public AdmissionTrainingProgram toDomain(AdmissionTrainingProgramDto dto) {
        return null;
    }
}
