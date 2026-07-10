package com.appgym.booking.repository;

import com.appgym.booking.domain.Activity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivityRepository extends JpaRepository<Activity, UUID> {

    List<Activity> findByBusinessIdAndActiveTrueOrderByNameAsc(UUID businessId);

    Optional<Activity> findByIdAndBusinessId(UUID id, UUID businessId);
}
