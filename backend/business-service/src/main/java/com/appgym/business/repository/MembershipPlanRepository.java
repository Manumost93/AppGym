package com.appgym.business.repository;

import com.appgym.business.domain.MembershipPlan;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MembershipPlanRepository extends JpaRepository<MembershipPlan, UUID> {

    List<MembershipPlan> findByBusinessIdOrderByCreatedAtAsc(UUID businessId);

    Optional<MembershipPlan> findByIdAndBusinessId(UUID id, UUID businessId);
}
