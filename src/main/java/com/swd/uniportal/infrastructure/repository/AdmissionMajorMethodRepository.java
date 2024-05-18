package com.swd.uniportal.infrastructure.repository;

import com.swd.uniportal.domain.admission.AdmissionMajorMethod;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AdmissionMajorMethodRepository extends JpaRepository<AdmissionMajorMethod, Long> {

    @Query(
            value = """
                    SELECT e FROM AdmissionMajorMethod e
                    LEFT JOIN FETCH e.admissionMethod
                    LEFT JOIN FETCH e.subjectGroups
                    WHERE e.admissionMajor.id = ?1 AND (e.name IS NULL OR e.name LIKE CONCAT('%', ?2, '%'))
                    """,
            countQuery = """
                        SELECT COUNT(e) FROM AdmissionMajorMethod e
                        WHERE e.admissionMajor.id = ?1 AND (e.name IS NULL OR e.name LIKE CONCAT('%', ?2, '%'))
                        """
    )
    List<AdmissionMajorMethod> getBySearch(Long admissionMajorId, String search, Pageable pageable);

    @Query(
            value = """
                    SELECT COUNT(e) FROM AdmissionMajorMethod e
                    WHERE e.admissionMajor.id = ?1 AND (e.name IS NULL OR e.name LIKE CONCAT('%', ?2, '%'))
                    """
    )
    Integer countBySearch(Long admissionMajorId, String search);

    @Query(
            value = """
                    SELECT e FROM AdmissionMajorMethod e
                    LEFT JOIN FETCH e.subjectGroups
                    LEFT JOIN FETCH e.admissionMajor
                    WHERE e.id = ?1
                    """
    )
    Optional<AdmissionMajorMethod> getWithSubjectGroupsPopulated(Long admissionMajorMethodId);
}