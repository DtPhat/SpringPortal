package com.swd.uniportal.infrastructure.repository;

import com.swd.uniportal.domain.admission.AdmissionMethod;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AdmissionMethodRepository extends JpaRepository<AdmissionMethod, Long> {

    @Query(
            value = """
                    SELECT e FROM AdmissionMethod e
                    WHERE e.name LIKE CONCAT('%', ?1, '%')
                    """,
            countQuery = """
                        SELECT COUNT(e) FROM AdmissionMethod e
                        WHERE e.name LIKE CONCAT('%', ?1, '%')
                        """
    )
    List<AdmissionMethod> getBySearch(String search, Pageable pageable);

    @Query(
            value = """
                    SELECT COUNT(e) FROM AdmissionMethod e
                    WHERE e.name LIKE CONCAT('%', ?1, '%')
                    """
    )
    Integer countBySearch(String search);

    @Query(
            value = """
                    SELECT e FROM AdmissionMethod e
                    WHERE e.name LIKE CONCAT('%', ?1, '%')
                    """
    )
    List<AdmissionMethod> getBySearch(String search);
}