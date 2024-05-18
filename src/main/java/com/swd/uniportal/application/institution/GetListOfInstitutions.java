package com.swd.uniportal.application.institution;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.swd.uniportal.application.common.BaseController;
import com.swd.uniportal.application.common.FailedResponse;
import com.swd.uniportal.domain.institution.Institution;
import com.swd.uniportal.domain.institution.QInstitution;
import com.swd.uniportal.infrastructure.common.SortOrder;
import com.swd.uniportal.infrastructure.common.annotation.Datasource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityManager;
import java.util.List;
import lombok.AccessLevel;
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
public class GetListOfInstitutions {

    @Builder
    public record GetInstitutionsRequest(String search, SortOrder sortOrder, Long page, boolean all) {

    }

    @Builder
    public record InstitutionsResponse(Long page, Long totalPages, Long pageSize, Long currentPageSize,
                                       List<InstitutionResponse> institutions) {

    }

    @Builder
    public record InstitutionResponse(Long id, String name, String code, String avatarLink) {

    }

    @RestController
    @Tag(name = "institutions")
    public static final class GetListOfInstitutionsController extends BaseController {

        private final GetListOfInstitutionsService service;

        public GetListOfInstitutionsController(GetListOfInstitutionsService service) {
            this.service = service;
        }

        @GetMapping("/institutions")
        @Operation(summary = "Get list of institutions.")
        @ApiResponse(
                responseCode = "200",
                description = "Founded.",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = InstitutionsResponse.class)
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
                InstitutionsResponse response = service.get(GetInstitutionsRequest.builder()
                        .search(StringUtils.trimToNull(search))
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
    public static final class GetListOfInstitutionsService {

        @Value("${uniportal.pagination.size}")
        private Long pageSize;

        private final GetListOfInstitutionsDatasource datasource;

        public GetListOfInstitutionsService(GetListOfInstitutionsDatasource datasource) {
            this.datasource = datasource;
        }

        public InstitutionsResponse get(GetInstitutionsRequest request) {
            List<Institution> institutions = datasource.getInstitutions(request);
            long count = datasource.getCountOfInstitutions(request);
            Long totalPages = Math.ceilDiv(count, request.all() ? count : pageSize);
            return InstitutionsResponse.builder()
                    .page(request.page())
                    .totalPages(totalPages)
                    .pageSize(request.all() ? count : pageSize)
                    .currentPageSize((long) institutions.size())
                    .institutions(institutions.stream().map(institution -> InstitutionResponse.builder()
                            .id(institution.getId())
                            .name(institution.getName())
                            .code(institution.getCode())
                            .avatarLink(institution.getAvatarLink())
                            .build()).toList())
                    .build();
        }

    }

    @Datasource
    public static final class GetListOfInstitutionsDatasource {

        @Value("${uniportal.pagination.size}")
        private Long pageSize;

        private final EntityManager entityManager;

        public GetListOfInstitutionsDatasource(EntityManager entityManager) {
            this.entityManager = entityManager;
        }

        public List<Institution> getInstitutions(GetInstitutionsRequest request) {
            QInstitution institution = QInstitution.institution;
            JPAQueryFactory factory = new JPAQueryFactory(entityManager);
            BooleanBuilder filters = new BooleanBuilder();
            if (StringUtils.isNotBlank(request.search)) {
                filters.and(institution.name.containsIgnoreCase(request.search()));
            }
            if (request.all()) {
                return factory.selectFrom(institution)
                        .where(filters)
                        .orderBy((request.sortOrder() == SortOrder.DESC) ? institution.name.desc() : institution.name.asc())
                        .fetch();
            }
            int offset = (int) ((request.page - 1) * pageSize);
            return factory.selectFrom(institution)
                    .where(filters)
                    .orderBy((request.sortOrder() == SortOrder.DESC) ? institution.name.desc() : institution.name.asc())
                    .offset(offset)
                    .limit(pageSize)
                    .fetch();
        }

        public long getCountOfInstitutions(GetInstitutionsRequest request) {
            QInstitution institution = QInstitution.institution;
            JPAQueryFactory factory = new JPAQueryFactory(entityManager);
            BooleanBuilder filters = new BooleanBuilder();
            if (StringUtils.isNotBlank(request.search)) {
                filters.and(institution.name.containsIgnoreCase(request.search()));
            }
            return factory.selectFrom(institution)
                    .where(filters)
                    .orderBy((request.sortOrder() == SortOrder.DESC) ? institution.name.desc() : institution.name.asc())
                    .stream().count();
        }

    }

}
