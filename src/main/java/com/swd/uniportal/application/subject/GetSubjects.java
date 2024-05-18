package com.swd.uniportal.application.subject;

import com.swd.uniportal.application.common.BaseController;
import com.swd.uniportal.application.common.FailedResponse;
import com.swd.uniportal.application.subject.dto.SubjectDto;
import com.swd.uniportal.domain.subject.Subject;
import com.swd.uniportal.infrastructure.common.annotation.Datasource;
import com.swd.uniportal.infrastructure.repository.SubjectRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class GetSubjects {

    @Data
    @Builder
    public static final class SubjectsResponse {

        private Integer page;
        private Integer totalPages;
        private Integer pageSize;
        private Integer size;
        private List<SubjectDto> subjects;
    }

    @RestController
    @Tag(name = "subjects")
    public static final class GetSubjectsController extends BaseController {

        private final GetSubjectsService service;

        @Autowired
        public GetSubjectsController(GetSubjectsService service) {
            this.service = service;
        }

        @GetMapping("/subjects")
        @Operation(summary = "Get list of subjects.")
        @ApiResponse(
                responseCode = "200",
                description = "Founded.",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = SubjectsResponse.class)
                )
        )
        @ApiResponse(
                responseCode = "400",
                description = "Invalid parameters.",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = FailedResponse.class)
                )
        )
        @ApiResponse(
                responseCode = "403",
                description = "No permission to get subjects list with current role.",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = FailedResponse.class)
                )
        )
        @ApiResponse(
                responseCode = "500",
                description = "Server error.",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = FailedResponse.class)
                )
        )
        public ResponseEntity<Object> get(
                @RequestParam(name = "search", defaultValue = "") String search,
                @RequestParam(name = "page", defaultValue = "1") Integer page) {
            if (page < 1) {
                return ResponseEntity.badRequest().body(new FailedResponse(List.of("Page is not positive.")));
            }
            try {
                SubjectsResponse response = service.getSubjectsBySearch(StringUtils.trimToEmpty(search), page);
                return ResponseEntity.ok(response);
            } catch (Exception e) {
                return ResponseEntity.internalServerError().body(new FailedResponse(List.of("Server error.")));
            }
        }
    }

    @Service
    public static class GetSubjectsService {

        @Value("${uniportal.pagination.size}")
        private Integer pageSize;

        private final GetSubjectsDatasource datasource;

        @Autowired
        public GetSubjectsService(GetSubjectsDatasource datasource) {
            this.datasource = datasource;
        }

        public SubjectsResponse getSubjectsBySearch(String search, Integer page) {
            Pageable pageable = PageRequest.of(page - 1, pageSize);
            List<Subject> subjects = datasource.getSubjectsBySearch(search, pageable);
            Integer subjectsCount = datasource.countSubjectsBySearch(search);
            return SubjectsResponse.builder()
                    .page(page)
                    .totalPages(Math.ceilDiv(subjectsCount, pageSize))
                    .pageSize(pageSize)
                    .size(subjects.size())
                    .subjects(subjects.stream()
                            .map(s -> SubjectDto.builder()
                                    .id(s.getId())
                                    .name(s.getName())
                                    .description(s.getDescription())
                                    .build())
                            .toList())
                    .build();
        }
    }

    @Datasource
    public static class GetSubjectsDatasource {

        private final SubjectRepository subjectRepository;

        @Autowired
        public GetSubjectsDatasource(SubjectRepository subjectRepository) {
            this.subjectRepository = subjectRepository;
        }

        public List<Subject> getSubjectsBySearch(String search, Pageable pageable) {
            return subjectRepository.findSubjectsByNameContainsIgnoreCase(search, pageable);
        }

        public Integer countSubjectsBySearch(String search) {
            return subjectRepository.countSubjectsByNameContainsIgnoreCase(search);
        }
    }
}
