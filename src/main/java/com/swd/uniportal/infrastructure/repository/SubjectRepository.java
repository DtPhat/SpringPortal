package com.swd.uniportal.infrastructure.repository;

import com.swd.uniportal.domain.subject.Subject;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubjectRepository extends JpaRepository<Subject, Long> {

    List<Subject> findSubjectsByNameContainsIgnoreCase(String search, Pageable pageable);

    Integer countSubjectsByNameContainsIgnoreCase(String search);

}