package com.swd.uniportal.application.student.dto;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public final class StudentRecordsDto {

    private Integer size;
    private List<StudentRecordDto> studentRecords;
}
