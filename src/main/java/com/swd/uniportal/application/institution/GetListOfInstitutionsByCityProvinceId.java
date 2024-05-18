package com.swd.uniportal.application.institution;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.swd.uniportal.application.common.BaseController;
import com.swd.uniportal.application.common.FailedResponse;
import com.swd.uniportal.domain.address.QAddress;
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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GetListOfInstitutionsByCityProvinceId {

    @Builder
    public record GetInstitutionsByCityProvinceIdRequest(String search, SortOrder sortOrder, Long page, Long cityProvinceId) {}

    @Builder
    public record InstitutionsResponse(Long page, Long totalPages, Long pageSize, Long currentPageSize,
                                       List<InstitutionResponse> institutions) {
    }

    @Builder
    public record InstitutionResponse(Long id, String name, String code, String avatarLink) {
    }

    @RestController
    @Tag(name = "institutions")
    @AllArgsConstructor
    public static final class GetListOfInstitutionsByCityProvinceIdController extends BaseController {

        private final GetListOfInstitutionsByCityProvinceIdService service;

        @GetMapping("/institutions/city-provinces/{id}")
        @Operation(summary = "Get list of institutions by city province ID.")
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
                @PathVariable("id") Long cityProvinceId,
                @RequestParam(name = "search", required = false) String search,
                @RequestParam(name = "sort", defaultValue = "ASC") SortOrder sortOrder,
                @RequestParam(name = "page", defaultValue = "1") Long page) {
            try {
                if (page < 1) {
                    return ResponseEntity.badRequest().body(new FailedResponse(List
                            .of("Page must be positive (page > 0)")));
                }
                InstitutionsResponse response = service.get(GetInstitutionsByCityProvinceIdRequest.builder()
                        .cityProvinceId(cityProvinceId)
                        .search(StringUtils.trimToNull(search))
                        .sortOrder(sortOrder)
                        .page(page)
                        .build());
                return ResponseEntity.ok(response);
            } catch (Exception e) {
                return ResponseEntity.internalServerError()
                        .body(new FailedResponse(List.of("Server error.", e.getMessage())));
            }
        }
    }

    @Service
    public static final class GetListOfInstitutionsByCityProvinceIdService {

        @Value("${uniportal.pagination.size}")
        private Long pageSize;

        private final GetListOfInstitutionsByCityProvinceIdDatasource datasource;

        public GetListOfInstitutionsByCityProvinceIdService(
                GetListOfInstitutionsByCityProvinceIdDatasource datasource) {
            this.datasource = datasource;
        }

        public InstitutionsResponse get(
                GetInstitutionsByCityProvinceIdRequest request) {
            List<Institution> institutions = datasource.getInstitutionsByCityProvinceId(request);
            Long totalPages = (long) Math.ceil((double) datasource.getCountOfInstitutions(request) / pageSize);
            return InstitutionsResponse.builder()
                    .page(request.page())
                    .totalPages(totalPages)
                    .pageSize(pageSize)
                    .currentPageSize((long) institutions.size())
                    .institutions(institutions.stream().map(institution -> InstitutionResponse.builder()
                                    .code(institution.getCode())
                                    .name(institution.getName())
                                    .id(institution.getId())
                                    .avatarLink(institution.getAvatarLink())
                                    .build())
                            .toList())
                    .build();
        }
    }

    @Datasource
    public static final class GetListOfInstitutionsByCityProvinceIdDatasource {

        @Value("${uniportal.pagination.size}")
        private Long pageSize;

        private final EntityManager entityManager;

        public GetListOfInstitutionsByCityProvinceIdDatasource(EntityManager entityManager) {
            this.entityManager = entityManager;
        }

        @SuppressWarnings("Duplicates")
        public List<Institution> getInstitutionsByCityProvinceId(GetInstitutionsByCityProvinceIdRequest request) {
            QInstitution institution = QInstitution.institution;
            QAddress address = QAddress.address;

            JPAQueryFactory factory = new JPAQueryFactory(entityManager);

            BooleanBuilder filters = new BooleanBuilder();
            if (StringUtils.isNotBlank(request.search)) {
                filters.and(institution.name.containsIgnoreCase(request.search()));
            }
            int offset = (int) ((request.page - 1) * pageSize);

            return factory.selectDistinct(institution)
                    .from(address)
                    .leftJoin(address.institution, institution)
                    .where(address.cityProvince.id.eq(request.cityProvinceId))
                    .where(filters)
                    .orderBy((request.sortOrder() == SortOrder.DESC) ? institution.name.desc() : institution.name.asc())
                    .offset(offset)
                    .limit(pageSize)
                    .fetch();
        }

        @SuppressWarnings("Duplicates")
        public long getCountOfInstitutions(GetInstitutionsByCityProvinceIdRequest request) {
            QInstitution institution = QInstitution.institution;
            QAddress address = QAddress.address;

            JPAQueryFactory factory = new JPAQueryFactory(entityManager);

            BooleanBuilder filters = new BooleanBuilder();
            if (StringUtils.isNotBlank(request.search)) {
                filters.and(institution.name.containsIgnoreCase(request.search()));
            }
            return factory.selectDistinct(institution)
                    .from(address)
                    .leftJoin(address.institution, institution)
                    .where(address.cityProvince.id.eq(request.cityProvinceId))
                    .where(filters)
                    .orderBy((request.sortOrder() == SortOrder.DESC) ? institution.name.desc() : institution.name.asc())
                    .stream().count();
        }

    }
}
