package com.swd.uniportal.infrastructure.repository;

import com.swd.uniportal.domain.major.Major;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MajorRepository extends JpaRepository<Major, Long> {

    @Query("SELECT m FROM Major m JOIN FETCH m.department d JOIN FETCH d.school")
    Page<Major> findAllWithDepartmentAndSchool(Pageable pageable);

    Boolean existsMajorByCode(String code);

    @Query(
            value = """
                    SELECT e FROM Major e
                    LEFT JOIN FETCH e.department d
                    LEFT JOIN FETCH d.school
                    WHERE e.id = ?1
                    """
    )
    Optional<Major> findByIdPopulated(Long id);
}