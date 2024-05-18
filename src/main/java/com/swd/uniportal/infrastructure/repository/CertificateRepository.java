package com.swd.uniportal.infrastructure.repository;

import com.swd.uniportal.domain.student.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CertificateRepository extends JpaRepository<Certificate, Long> {

}