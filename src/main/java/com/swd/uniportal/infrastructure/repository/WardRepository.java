package com.swd.uniportal.infrastructure.repository;

import com.swd.uniportal.domain.address.Ward;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface WardRepository extends JpaRepository<Ward, Long> {

    @Query(
            value = """
                    SELECT e FROM Ward e
                    WHERE e.name LIKE CONCAT('%', ?1, '%')
                    """
    )
    List<Ward> getAllBySearchSorted(String search, Sort sort);
}