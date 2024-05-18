package com.swd.uniportal.infrastructure.repository;

import com.swd.uniportal.domain.admission.AdmissionMajor;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AdmissionMajorRepository extends JpaRepository<AdmissionMajor, Long> {

    @Query(
            value = """
                    SELECT e FROM AdmissionMajor e
                    LEFT JOIN FETCH e.admissionTrainingProgram atp
                    LEFT JOIN FETCH atp.trainingProgram
                    LEFT JOIN FETCH e.admissionMajorMethods amm
                    LEFT JOIN FETCH amm.admissionMethod
                    LEFT JOIN FETCH amm.subjectGroups
                    LEFT JOIN FETCH e.major
                    WHERE e.admissionPlan.id = ?1 AND e.name LIKE CONCAT('%', ?2, '%')
                    """,
            countQuery = """
                        SELECT COUNT(e) FROM AdmissionMajor e
                        WHERE e.admissionPlan.id = ?1 AND e.name LIKE CONCAT('%', ?2, '%')
                        """
    )
    List<AdmissionMajor> getAdmissionMajorsBySearch(Long admissionId, String search, Pageable pageable);

    @Query(
            value = """
                    SELECT COUNT(e) FROM AdmissionMajor e
                    WHERE e.admissionPlan.id = ?1 AND e.name LIKE CONCAT('%', ?2, '%')
                    """
    )
    Integer countAllBySearch(Long admissionId, String search);

    boolean existsByAdmissionTrainingProgramId(Long admissionTrainingProgramId);

    @Query(
            value = """
                    SELECT e FROM AdmissionMajor e
                    LEFT JOIN FETCH e.admissionMajorMethods
                    WHERE e.id = ?1
                    """
    )
    Optional<AdmissionMajor> getWithMajorMethodsPopulated(Long admissionMajorId);
}