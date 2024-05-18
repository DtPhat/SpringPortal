package com.swd.uniportal.infrastructure.repository;

import com.swd.uniportal.domain.address.District;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DistrictRepository extends JpaRepository<District, Long> {

    @Query("SELECT d FROM District d LEFT JOIN FETCH d.wards WHERE d.id = :id")
    Optional<District> findByIdWithWards(@Param("id") Long id);

}