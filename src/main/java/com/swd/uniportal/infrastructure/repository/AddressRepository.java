package com.swd.uniportal.infrastructure.repository;

import com.swd.uniportal.domain.address.Address;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository extends JpaRepository<Address, Long> {

}