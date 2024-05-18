package com.swd.uniportal.application.admission.mapper;

import com.swd.uniportal.application.admission.dto.TrainingProgramDto;
import com.swd.uniportal.application.common.Mapper;
import com.swd.uniportal.domain.admission.TrainingProgram;
import java.util.Objects;
import org.springframework.stereotype.Component;

@Component
public class TrainingProgramMapper implements Mapper<TrainingProgram, TrainingProgramDto> {

    @Override
    public TrainingProgramDto toDto(TrainingProgram domain) {
        if (Objects.isNull(domain)) {
            return null;
        }
        return TrainingProgramDto.builder()
                .id(domain.getId())
                .name(domain.getName())
                .build();
    }

    @Override
    public TrainingProgram toDomain(TrainingProgramDto dto) {
        return null;
    }
}
