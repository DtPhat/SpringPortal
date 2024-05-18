package com.swd.uniportal.infrastructure.repository;

import com.swd.uniportal.domain.institution.Institution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface InstitutionRepository extends JpaRepository<Institution, Long> {

    @Query("SELECT i FROM Institution i LEFT JOIN FETCH i.addresses WHERE i.id = ?1")
    Institution findByIdWithAddresses(Long id);

}