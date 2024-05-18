package com.swd.uniportal.infrastructure.repository;

import com.swd.uniportal.domain.admission.AdmissionPlan;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AdmissionPlanRepository extends JpaRepository<AdmissionPlan, Long> {

    @Query(
            value = """
                    SELECT e FROM AdmissionPlan e
                    LEFT JOIN FETCH e.institution i
                    LEFT JOIN FETCH e.admissionMajors am
                    LEFT JOIN FETCH am.major
                    LEFT JOIN FETCH am.admissionTrainingProgram atp
                    LEFT JOIN FETCH atp.trainingProgram
                    LEFT JOIN FETCH am.admissionMajorMethods amm
                    LEFT JOIN FETCH amm.admissionMethod
                    LEFT JOIN FETCH amm.subjectGroups
                    WHERE e.id = ?1
                    """
    )
    Optional<AdmissionPlan> getByIdPopulated(Long id);

    @Query(
            value = """
                    SELECT e FROM AdmissionPlan e
                    LEFT JOIN FETCH e.admissionTrainingPrograms
                    WHERE e.id = ?1
                    """
    )
    Optional<AdmissionPlan> getByIdTrainingProgramsPopulated(Long id);

    @Query(
            value = """
                    SELECT e FROM AdmissionPlan e
                    LEFT JOIN FETCH e.admissionTrainingPrograms
                    LEFT JOIN FETCH e.admissionMajors
                    WHERE e.id = ?1
                    """
    )
    Optional<AdmissionPlan> getAdmissionPlanMajorsTrainingProgramsPopulated(Long id);

    @Query(
            value = """
                    SELECT e FROM AdmissionPlan e
                    LEFT JOIN FETCH Institution i ON e.institution.id = i.id
                    WHERE e.name LIKE CONCAT('%', ?1, '%')
                    """,
            countQuery = """
                        SELECT COUNT (*) FROM AdmissionPlan e
                        WHERE e.name LIKE CONCAT('%', ?1, '%')
                        """
    )
    List<AdmissionPlan> getAdmissionPlansBySearch(String search, Pageable pageable);

    @Query(
            value = """
                    SELECT COUNT (*) FROM AdmissionPlan e
                    WHERE e.name LIKE CONCAT('%', ?1, '%')
                    """
    )
    Integer countAdmissionPlansBySearch(String search);

    @Query(
            value = """
                    SELECT e FROM AdmissionPlan e
                    LEFT JOIN FETCH e.institution
                    WHERE e.name LIKE CONCAT('%', ?1, '%') AND e.institution.id = ?2
                    """,
            countQuery = """
                        SELECT COUNT (*) FROM AdmissionPlan e
                        WHERE e.name LIKE CONCAT('%', ?1, '%') AND e.institution.id = ?2
                        """
    )
    List<AdmissionPlan> getAdmissionPlansBySearchAndInstitutionId(String search, Long institutionId, Pageable pageable);

    @Query(
            value = """
                    SELECT COUNT (*) FROM AdmissionPlan e
                    WHERE e.name LIKE CONCAT('%', ?1, '%') AND e.institution.id = ?2
                    """
    )
    Integer countAdmissionPlansBySearchAndInstitutionId(String search, Long institutionId);
}