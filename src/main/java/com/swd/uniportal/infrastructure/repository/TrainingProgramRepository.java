package com.swd.uniportal.infrastructure.repository;

import com.swd.uniportal.domain.admission.TrainingProgram;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TrainingProgramRepository extends JpaRepository<TrainingProgram, Long> {

    @Query(
            value = """
                    SELECT e FROM TrainingProgram e
                    WHERE e.name LIKE CONCAT('%', ?1, '%')
                    """,
            countQuery = """
                        SELECT COUNT(e) FROM TrainingProgram e
                        WHERE e.name LIKE CONCAT('%', ?1, '%')
                        """
    )
    List<TrainingProgram> getTrainingProgramsBySearch(String search, Pageable pageable);

    @Query(
            value = """
                    SELECT COUNT(e) FROM TrainingProgram e
                    WHERE e.name LIKE CONCAT('%', ?1, '%')
                    """
    )
    Integer countTrainingProgramsBySearch(String search);

    @Query(
            value = """
                    SELECT CASE
                        WHEN COUNT(tp) != ?2 THEN TRUE
                        ELSE FALSE
                        END
                    FROM TrainingProgram tp
                    WHERE tp.id IN ?1
                    """
    )
    Boolean existsInvalidTrainingProgram(List<Long> trainingProgramIds, Integer size);
}