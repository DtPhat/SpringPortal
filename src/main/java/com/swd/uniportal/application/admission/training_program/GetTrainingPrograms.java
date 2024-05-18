package com.swd.uniportal.application.admission.training_program;

import com.swd.uniportal.application.admission.dto.TrainingProgramDto;
import com.swd.uniportal.application.common.BaseController;
import com.swd.uniportal.application.common.FailedResponse;
import com.swd.uniportal.application.common.Mapper;
import com.swd.uniportal.domain.admission.TrainingProgram;
import com.swd.uniportal.infrastructure.common.annotation.Datasource;
import com.swd.uniportal.infrastructure.repository.TrainingProgramRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
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
public final class GetTrainingPrograms {

    @Data
    @Builder
    public static final class TrainingProgramsDto {

        Integer page;
        Integer totalPages;
        Integer pageSize;
        Integer size;
        List<TrainingProgramDto> trainingPrograms;
    }

    @RestController
    @Tag(name = "admission-plans")
    public static final class GetTrainingProgramsController extends BaseController {

        private final GetTrainingProgramsService service;

        @Autowired
        public GetTrainingProgramsController(GetTrainingProgramsService service) {
            this.service = service;
        }

        @GetMapping("/admission-plans/training-programs")
        @Operation(summary = "Get training programs.")
        @ApiResponse(
                responseCode = "200",
                description = "Successful.",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = TrainingProgramsDto.class)
                )
        )
        @ApiResponse(
                responseCode = "400",
                description = "Invalid request.",
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
            Integer pageToUse = page;
            if (Objects.isNull(pageToUse) || pageToUse < 1) {
                pageToUse = 1;
            }
            try {
                TrainingProgramsDto trainingPrograms = service.get(search, pageToUse);
                return ResponseEntity.ok(trainingPrograms);
            } catch (Exception e) {
                return ResponseEntity.internalServerError()
                        .body(new FailedResponse(List.of("Server error.")));
            }
        }
    }

    @Service
    public static final class GetTrainingProgramsService {

        private final GetTrainingProgramsDatasource datasource;
        private final Mapper<TrainingProgram, TrainingProgramDto> mapper;

        @Value("${uniportal.pagination.size}")
        private Integer pageSize;

        @Autowired
        public GetTrainingProgramsService(GetTrainingProgramsDatasource datasource,
                                          Mapper<TrainingProgram, TrainingProgramDto> mapper) {
            this.datasource = datasource;
            this.mapper = mapper;
        }

        public TrainingProgramsDto get(String search, Integer pageToUse) {
            Pageable pageable = PageRequest.of(pageToUse - 1, pageSize);
            List<TrainingProgram> trainingPrograms = datasource.get(search, pageable);
            List<TrainingProgramDto> mapped = trainingPrograms.stream()
                    .map(mapper::toDto)
                    .toList();
            return TrainingProgramsDto.builder()
                    .page(pageToUse)
                    .totalPages(Math.ceilDiv(datasource.countTrainingProgramsBySearch(search), pageSize))
                    .size(mapped.size())
                    .pageSize(pageSize)
                    .trainingPrograms(mapped)
                    .build();
        }
    }

    @Datasource
    public static final class GetTrainingProgramsDatasource {

        private final TrainingProgramRepository trainingProgramRepository;

        @Autowired
        public GetTrainingProgramsDatasource(TrainingProgramRepository trainingProgramRepository) {
            this.trainingProgramRepository = trainingProgramRepository;
        }

        public List<TrainingProgram> get(String search, Pageable pageable) {
            return trainingProgramRepository.getTrainingProgramsBySearch(search, pageable);
        }

        public Integer countTrainingProgramsBySearch(String search) {
            return trainingProgramRepository.countTrainingProgramsBySearch(search);
        }
    }
}
