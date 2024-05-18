package com.swd.uniportal.infrastructure.repository;

import com.swd.uniportal.domain.student.StudentRecord;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface StudentRecordRepository extends JpaRepository<StudentRecord, Long> {

    @Query(
            value = """
                    SELECT CASE
                        WHEN COUNT(e) > 0 THEN TRUE
                        ELSE FALSE
                    END
                    FROM StudentRecord e
                    WHERE e.student.account.id = ?1
                    """
    )
    Boolean existsByIdAndAccount(Long accountId);

    @Query(
            value = """
                    SELECT e FROM StudentRecord e
                    LEFT JOIN FETCH e.student
                    LEFT JOIN FETCH e.subject
                    WHERE e.student.account.id = ?1
                    """
    )
    List<StudentRecord> getByAccount(Long accountId);
}