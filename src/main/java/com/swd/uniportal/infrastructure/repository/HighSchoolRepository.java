package com.swd.uniportal.infrastructure.repository;

import com.swd.uniportal.domain.institution.HighSchool;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface HighSchoolRepository extends JpaRepository<HighSchool, Long> {

    @Query(
            value = """
                    SELECT e FROM HighSchool e
                    LEFT JOIN FETCH e.cityProvince
                    WHERE e.id = ?1
                    """
    )
    Optional<HighSchool> getByIdPopulated(Long id);

    boolean existsByNameIgnoreCaseAndCityProvinceId(String name, Long cityProvinceId);

    @Query(
            value = """
                    SELECT e FROM HighSchool e
                    LEFT JOIN FETCH e.cityProvince
                    WHERE e.name LIKE CONCAT('%', ?1, '%')
                    """
    )
    List<HighSchool> getAllBySearch(String search);
}