package com.swd.uniportal.application.high_school;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.swd.uniportal.application.common.BaseController;
import com.swd.uniportal.application.common.FailedResponse;
import com.swd.uniportal.application.common.Mapper;
import com.swd.uniportal.application.high_school.dto.HighSchoolDto;
import com.swd.uniportal.application.high_school.dto.HighSchoolsDto;
import com.swd.uniportal.domain.institution.HighSchool;
import com.swd.uniportal.domain.institution.QHighSchool;
import com.swd.uniportal.infrastructure.common.SortOrder;
import com.swd.uniportal.infrastructure.common.annotation.Datasource;
import com.swd.uniportal.infrastructure.repository.HighSchoolRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityManager;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GetHighSchools {

    @Builder
    public record GetHighSchoolsRequest(String search, SortOrder sortOrder, Long page, boolean all) {
    }

    @RestController
    @Tag(name = "high-schools")
    @AllArgsConstructor
    public static final class GetListOfInstitutionsController extends BaseController {

        private final GetHighSchoolsService service;

        @GetMapping("/high-schools")
        @Operation(summary = "Get list of high schools.")
        @ApiResponse(
                responseCode = "200",
                description = "Founded.",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = HighSchoolsDto.class)
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
                responseCode = "500",
                description = "Server error.",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = FailedResponse.class)
                )
        )
        public ResponseEntity<Object> get(
                @RequestParam(name = "search", required = false) String search,
                @RequestParam(name = "sort", defaultValue = "ASC") SortOrder sortOrder,
                @RequestParam(name = "page", defaultValue = "1") Long page,
                @RequestParam(name = "all", defaultValue = "false") boolean all) {
            try {
                if (page < 1) {
                    return ResponseEntity.badRequest().body(new FailedResponse(List
                            .of("Page must be positive (page > 0)")));
                }
                HighSchoolsDto response = service.get(GetHighSchoolsRequest.builder()
                        .search(StringUtils.trimToEmpty(search))
                        .sortOrder(sortOrder)
                        .page(page)
                        .all(all)
                        .build());
                return ResponseEntity.ok(response);
            } catch (Exception e) {
                return ResponseEntity.internalServerError().body(new FailedResponse(List
                        .of("Server error.", e.getMessage())));
            }
        }

    }

    @Service
    public static final class GetHighSchoolsService {

        @Value("${uniportal.pagination.size}")
        private Long pageSize;

        private final GetHighSchoolsDataSource datasource;
        private final Mapper<HighSchool, HighSchoolDto> mapper;

        public GetHighSchoolsService(
                GetHighSchoolsDataSource datasource,
                Mapper<HighSchool, HighSchoolDto> mapper) {
            this.datasource = datasource;
            this.mapper = mapper;
        }

        public HighSchoolsDto get(GetHighSchoolsRequest request) {
            List<HighSchool> highSchools;
            if (request.all()) {
                highSchools = datasource.getAllHighSchools(request.search());
            } else {
                highSchools = datasource.getHighSchools(request);
            }
            long count = datasource.getCountOfHighSchools(request);
            long totalPages = (long) Math.ceil((double) count / pageSize);
            return HighSchoolsDto.builder()
                    .page(request.page())
                    .totalPages(request.all() ? 1 : totalPages)
                    .pageSize(request.all() ? count : pageSize)
                    .currentPageSize((long) highSchools.size())
                    .highSchools(highSchools.stream()
                            .map(mapper::toDto)
                            .toList())
                    .build();
        }

    }

    @Datasource
    public static final class GetHighSchoolsDataSource {

        @Value("${uniportal.pagination.size}")
        private Long pageSize;

        private final HighSchoolRepository highSchoolRepository;
        private final EntityManager entityManager;

        public GetHighSchoolsDataSource(
                HighSchoolRepository highSchoolRepository,
                EntityManager entityManager) {
            this.highSchoolRepository = highSchoolRepository;
            this.entityManager = entityManager;
        }

        public List<HighSchool> getHighSchools(GetHighSchoolsRequest request) {
            QHighSchool highSchool = QHighSchool.highSchool;
            JPAQueryFactory factory = new JPAQueryFactory(entityManager);
            BooleanBuilder filters = new BooleanBuilder();
            if (StringUtils.isNotBlank(request.search)) {
                filters.and(highSchool.name.containsIgnoreCase(request.search()));
            }
            int offset = (int) ((request.page - 1) * pageSize);
            return factory.selectFrom(highSchool)
                    .where(filters)
                    .orderBy((request.sortOrder() == SortOrder.DESC) ? highSchool.name.desc() : highSchool.name.asc())
                    .offset(offset)
                    .limit(pageSize)
                    .fetch();
        }

        public long getCountOfHighSchools(GetHighSchoolsRequest request) {
            QHighSchool highSchool = QHighSchool.highSchool;
            JPAQueryFactory factory = new JPAQueryFactory(entityManager);
            BooleanBuilder filters = new BooleanBuilder();
            if (StringUtils.isNotBlank(request.search)) {
                filters.and(highSchool.name.containsIgnoreCase(request.search()));
            }
            return factory.selectFrom(highSchool)
                    .where(filters)
                    .orderBy((request.sortOrder() == SortOrder.DESC) ? highSchool.name.desc() : highSchool.name.asc())
                    .stream().count();
        }

        public List<HighSchool> getAllHighSchools(String search) {
            return highSchoolRepository.getAllBySearch(search);
        }
    }
}
