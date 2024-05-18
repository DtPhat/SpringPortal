package com.swd.uniportal.application.address;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.swd.uniportal.application.common.BaseController;
import com.swd.uniportal.application.common.FailedResponse;
import com.swd.uniportal.domain.address.Address;
import com.swd.uniportal.domain.address.QAddress;
import com.swd.uniportal.infrastructure.common.SortOrder;
import com.swd.uniportal.infrastructure.common.annotation.Datasource;
import com.swd.uniportal.infrastructure.repository.AddressRepository;
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
public class GetListOfAddresses {

    @Builder
    public record GetAddressesRequest(String search, SortOrder sortOrder, Long page) {
    }

    @Builder
    public record AddressesResponse(Long page, Long totalPages, Long pageSize, Long currentPageSize,
                                    List<AddressResponse> addresses) {
    }

    @Builder
    public record AddressResponse(long id, String houseNumber, String streetName, String ward, String district,
                                  String cityProvince) {
    }

    @RestController
    @Tag(name = "addresses")
    public static final class GetListOfAddressesController extends BaseController {

        private final GetListOfAddressesService service;

        public GetListOfAddressesController(GetListOfAddressesService service) {
            this.service = service;
        }

        @GetMapping("/addresses")
        @Operation(summary = "Get list of addresses.")
        @ApiResponse(
                responseCode = "200",
                description = "Founded.",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = AddressesResponse.class)
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
                @RequestParam(name = "page", defaultValue = "1") Long page) {
            try {
                if (page < 1) {
                    return ResponseEntity.badRequest().body(new FailedResponse(List
                            .of("Page must be positive (page > 0)")));
                }
                AddressesResponse response = service.get(GetAddressesRequest.builder()
                        .search(StringUtils.trimToNull(search))
                        .sortOrder(sortOrder)
                        .page(page)
                        .build());
                return ResponseEntity.ok(response);
            } catch (Exception e) {
                e.printStackTrace(System.err);
                return ResponseEntity.internalServerError().body(new FailedResponse(List
                        .of("Server error.", e.getMessage())));
            }
        }

    }

    @Service
    public static final class GetListOfAddressesService {

        @Value("${uniportal.pagination.size}")
        private Long pageSize;

        private final GetListOfAddressesDatasource datasource;

        public GetListOfAddressesService(GetListOfAddressesDatasource datasource) {
            this.datasource = datasource;
        }

        public AddressesResponse get(GetAddressesRequest request) {
            List<Address> addresses = datasource.getAddresses(request);
            Long totalPages = (long) Math.ceil((double) datasource.getCountOfAddresses(request) / pageSize);
            return AddressesResponse.builder()
                    .page(request.page())
                    .totalPages(totalPages)
                    .pageSize(pageSize)
                    .currentPageSize((long) addresses.size())
                    .addresses(addresses.stream().map(address -> AddressResponse.builder()
                            .id(address.getId())
                            .houseNumber(address.getHouseNumber())
                            .streetName(address.getStreetName())
                            .ward(address.getWard().getName())
                            .district(address.getDistrict().getName())
                            .cityProvince(address.getCityProvince().getName())
                            .build()).toList())
                    .build();
        }

    }

    @Datasource
    public static final class GetListOfAddressesDatasource {

        @Value("${uniportal.pagination.size}")
        private Long pageSize;

        private final AddressRepository addressRepository;
        private final EntityManager entityManager;

        public GetListOfAddressesDatasource(AddressRepository addressRepository, EntityManager entityManager) {
            this.addressRepository = addressRepository;
            this.entityManager = entityManager;
        }

        public List<Address> getAddresses(GetAddressesRequest request) {
            QAddress address = QAddress.address;
            JPAQueryFactory factory = new JPAQueryFactory(entityManager);
            BooleanBuilder filters = new BooleanBuilder();
            if (StringUtils.isNotBlank(request.search)) {
                filters.and(address.streetName.containsIgnoreCase(request.search()));
            }
            int offset = (int) ((request.page - 1) * pageSize);
            return factory.selectFrom(address)
                    .where(filters)
                    .orderBy((request.sortOrder() == SortOrder.DESC) ? address.streetName.desc() : address.streetName.asc())
                    .offset(offset)
                    .limit(pageSize)
                    .fetch();
        }

        public long getCountOfAddresses(GetAddressesRequest request) {
            QAddress address = QAddress.address;
            JPAQueryFactory factory = new JPAQueryFactory(entityManager);
            BooleanBuilder filters = new BooleanBuilder();
            if (StringUtils.isNotBlank(request.search)) {
                filters.and(address.streetName.containsIgnoreCase(request.search()));
            }
            return factory.selectFrom(address)
                    .where(filters)
                    .orderBy((request.sortOrder() == SortOrder.DESC) ? address.streetName.desc() : address.streetName.asc())
                    .stream().count();
        }

    }

}
