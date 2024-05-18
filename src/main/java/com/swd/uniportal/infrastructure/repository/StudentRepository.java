package com.swd.uniportal.infrastructure.repository;

import com.swd.uniportal.domain.student.Student;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface StudentRepository extends JpaRepository<Student, Long> {

    @Query(
            value = """
                    SELECT e FROM Student e
                    WHERE e.account.id = ?1
                    """
    )
    Optional<Student> findByAccount(Long accountId);

    @Query(
            value = """
                    SELECT e FROM Student e
                    LEFT JOIN FETCH e.studentRecords
                    LEFT JOIN FETCH e.highSchool
                    LEFT JOIN FETCH e.account
                    WHERE e.account.id = ?1
                    """
    )
    Optional<Student> getByAccountPopulated(Long accountId);

    boolean existsByHighSchoolId(Long id);
}