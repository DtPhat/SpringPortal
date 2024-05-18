package com.swd.uniportal.application.admission.mapper;

import com.swd.uniportal.application.admission.dto.AdmissionMethodDto;
import com.swd.uniportal.application.common.Mapper;
import com.swd.uniportal.domain.admission.AdmissionMethod;
import org.springframework.stereotype.Component;

@Component
public final class AdmissionMethodMapper implements Mapper<AdmissionMethod, AdmissionMethodDto> {

    @Override
    public AdmissionMethodDto toDto(AdmissionMethod domain) {
        return AdmissionMethodDto.builder()
                .id(domain.getId())
                .name(domain.getName())
                .code(domain.getCode())
                .description(domain.getDescription())
                .build();
    }

    @Override
    public AdmissionMethod toDomain(AdmissionMethodDto dto) {
        return null;
    }
}
