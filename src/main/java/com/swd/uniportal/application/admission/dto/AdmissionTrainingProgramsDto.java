package com.swd.uniportal.application.admission.dto;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public final class AdmissionTrainingProgramsDto {

    private Integer page;
    private Integer totalPages;
    private Integer pageSize;
    private Integer size;
    private List<AdmissionTrainingProgramDto> admissionTrainingPrograms;
}
