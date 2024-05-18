package com.swd.uniportal.application.subject;

import com.swd.uniportal.application.common.BaseController;
import com.swd.uniportal.application.common.FailedResponse;
import com.swd.uniportal.application.subject.dto.SubjectGroupDto;
import com.swd.uniportal.domain.subject.Subject;
import com.swd.uniportal.domain.subject.SubjectGroup;
import com.swd.uniportal.infrastructure.common.annotation.Datasource;
import com.swd.uniportal.infrastructure.repository.SubjectGroupRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GetSubjectGroups {

    @Data
    @Builder
    public static final class SubjectGroupsResponse {

        Integer page;
        Integer totalPages;
        Integer pageSize;
        Integer size;
        List<SubjectGroupDto> subjectGroups;
    }

    @RestController
    @Tag(name = "subjects")
    public static class GetSubjectGroupsController extends BaseController {

        private final GetSubjectGroupsService service;

        @Autowired
        public GetSubjectGroupsController(GetSubjectGroupsService service) {
            this.service = service;
        }

        @GetMapping("/subjects/groups")
        @Operation(summary = "Get list of subject groups.")
        @ApiResponse(
                responseCode = "200",
                description = "Founded.",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = SubjectGroupsResponse.class)
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
                description = "No permission to get subject groups list with current role.",
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
                @RequestParam(name = "page", defaultValue = "1") Integer page,
                @RequestParam(name = "all", defaultValue = "false") boolean all) {
            if (page < 1) {
                return ResponseEntity.badRequest().body(new FailedResponse(List.of("Id is not positive.")));
            }
            SubjectGroupsResponse subjectGroupsResponse = service
                    .getSubjectGroups(StringUtils.trim(search), page, all);
            return new ResponseEntity<>(subjectGroupsResponse, HttpStatus.OK);
        }
    }

    @Service
    public static class GetSubjectGroupsService {

        @Value("${uniportal.pagination.size}")
        private Integer pageSize;

        private final GetSubjectGroupsDatasource datasource;

        @Autowired
        public GetSubjectGroupsService(GetSubjectGroupsDatasource datasource) {
            this.datasource = datasource;
        }

        public SubjectGroupsResponse getSubjectGroups(String search, Integer page, boolean all) {
            List<SubjectGroupDto> subjectGroupDto;
            if (all) {
                subjectGroupDto = datasource.getAllBySearch(search);
            } else {
                subjectGroupDto = datasource.getSubjectGroupsBySearch(search, page);
            }
            Integer count = datasource.countSubjectGroupsBySearch(search);
            return SubjectGroupsResponse.builder()
                    .page(page)
                    .totalPages(Math.ceilDiv(count, all ? count : pageSize))
                    .pageSize(all ? count : pageSize)
                    .size(subjectGroupDto.size())
                    .subjectGroups(subjectGroupDto)
                    .build();
        }
    }

    @Datasource
    public static class GetSubjectGroupsDatasource {

        @Value("${uniportal.pagination.size}")
        private Integer pageSize;

        private final SubjectGroupRepository subjectGroupRepository;

        @Autowired
        public GetSubjectGroupsDatasource(SubjectGroupRepository subjectGroupRepository) {
            this.subjectGroupRepository = subjectGroupRepository;
        }

        public List<SubjectGroupDto> getSubjectGroupsBySearch(String search, Integer page) {
            Pageable pageable = PageRequest.of(page - 1, pageSize);
            List<SubjectGroup> subjectGroups = subjectGroupRepository
                    .getSubjectGroupsBySearch(search, pageable);
            return subjectGroups.stream()
                    .map(sg -> SubjectGroupDto.builder()
                            .id(sg.getId())
                            .code(sg.getCode())
                            .subjects(sg.getSubjects().stream().map(Subject::getName).toList())
                            .build())
                    .toList();
        }

        public Integer countSubjectGroupsBySearch(String search) {
            return subjectGroupRepository.countSubjectGroupsBySearch(search);
        }

        public List<SubjectGroupDto> getAllBySearch(String search) {
            Set<SubjectGroup> subjectGroups = subjectGroupRepository
                    .getAllBySearch(search);
            return subjectGroups.stream()
                    .map(sg -> SubjectGroupDto.builder()
                            .id(sg.getId())
                            .code(sg.getCode())
                            .subjects(sg.getSubjects().stream().map(Subject::getName).toList())
                            .build())
                    .toList();
        }
    }
}
