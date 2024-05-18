package com.swd.uniportal.infrastructure.repository;

import com.swd.uniportal.domain.admission.AdmissionTrainingProgram;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AdmissionTrainingProgramRepository extends JpaRepository<AdmissionTrainingProgram, Long> {

    @Query(
            value = """
                    SELECT COUNT (e) FROM AdmissionTrainingProgram e
                    WHERE e.admissionPlan.id = ?1 AND e.name LIKE CONCAT('%', ?2, '%')
                    """
    )
    Integer countAdmissionTrainingProgramsBySearch(Long admissionId, String search);

    @Query(
            value = """
                    SELECT e FROM AdmissionTrainingProgram e
                    LEFT JOIN FETCH e.trainingProgram
                    WHERE e.admissionPlan.id = ?1 AND e.name LIKE CONCAT('%', ?2, '%')
                    """,
            countQuery = """
                        SELECT COUNT (e) FROM AdmissionTrainingProgram e
                        WHERE e.admissionPlan.id = ?1 AND e.name LIKE CONCAT('%', ?2, '%')
                        """
    )
    List<AdmissionTrainingProgram> getAdmissionTrainingProgramsByAdmissionPlanAndSearch(
            Long admissionId,
            String search,
            Pageable pageable);

    Optional<AdmissionTrainingProgram> getByIdAndAdmissionPlanId(Long id, Long admissionId);
}