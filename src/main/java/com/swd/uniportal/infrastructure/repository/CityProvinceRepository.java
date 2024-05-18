package com.swd.uniportal.infrastructure.repository;

import com.swd.uniportal.domain.address.CityProvince;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CityProvinceRepository extends JpaRepository<CityProvince, Long> {
    @Query("SELECT cp FROM CityProvince cp LEFT JOIN FETCH cp.districts WHERE cp.id = :id")
    Optional<CityProvince> findByIdWithDistricts(Long id);

    @Query(
            value = """
                    SELECT e FROM CityProvince e
                    WHERE e.name LIKE CONCAT('%', ?1, '%')
                    """
    )
    List<CityProvince> getAllBySearch(String search);
}