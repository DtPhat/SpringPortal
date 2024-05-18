package com.swd.uniportal.application.subject.dto;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public final class SubjectGroupDto {

    Long id;
    String code;
    List<String> subjects;
}
