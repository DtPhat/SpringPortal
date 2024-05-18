package com.swd.uniportal.infrastructure.repository;

import com.swd.uniportal.domain.subject.SubjectGroup;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SubjectGroupRepository extends JpaRepository<SubjectGroup, Long> {

    @Query(
            value = """
                    SELECT COUNT(e) FROM SubjectGroup e
                    WHERE e.code LIKE CONCAT('%', ?1, '%')
                    """
    )
    Integer countSubjectGroupsBySearch(String search);

    @Query(
            value = """
                    SELECT e FROM SubjectGroup e
                    LEFT JOIN FETCH e.subjects
                    WHERE e.code LIKE CONCAT('%', ?1, '%')
                    """,
            countQuery =
                        """
                        SELECT COUNT(e) FROM SubjectGroup e
                        WHERE e.code LIKE CONCAT('%', ?1, '%')
                        """
    )
    List<SubjectGroup> getSubjectGroupsBySearch(String search, Pageable pageable);

    Set<SubjectGroup> getByIdIn(List<Long> subjectGroupIds);

    @Query(
            value = """
                    SELECT e FROM SubjectGroup e
                    LEFT JOIN FETCH e.subjects
                    WHERE e.code LIKE CONCAT('%', ?1, '%')
                    """
    )
    Set<SubjectGroup> getAllBySearch(String search);
}