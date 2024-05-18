package com.swd.uniportal.infrastructure.repository;

import com.swd.uniportal.domain.major.School;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SchoolRepository extends JpaRepository<School, Long> {

    Boolean existsSchoolByCode(String code);

    @Query("SELECT s FROM School s LEFT JOIN FETCH s.departments WHERE s.id = ?1")
    Optional<School> findByIdWithDepartments(Long id);

}