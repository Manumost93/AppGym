package com.appgym.business.repository;

import com.appgym.business.domain.Business;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BusinessRepository extends JpaRepository<Business, UUID> {
}
