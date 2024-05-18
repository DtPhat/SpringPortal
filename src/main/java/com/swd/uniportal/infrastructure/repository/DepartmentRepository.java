package com.swd.uniportal.infrastructure.repository;

import com.swd.uniportal.domain.major.Department;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;


public interface DepartmentRepository extends JpaRepository<Department, Long> {

    Boolean existsDepartmentByCode(String code);

    @Query("SELECT d FROM Department d LEFT JOIN FETCH d.majors WHERE d.id = ?1")
    Optional<Department> findByIdWithMajors(Long id);

    @Query(
            value = """
                    SELECT e FROM Department e
                    WHERE e.name LIKE CONCAT('%', ?1, '%')
                    """
    )
    List<Department> getAllBySearchOrdered(String search, Sort sort);
}