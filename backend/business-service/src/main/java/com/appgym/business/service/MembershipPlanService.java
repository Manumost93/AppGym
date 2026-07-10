package com.appgym.business.service;

import com.appgym.business.domain.MembershipPlan;
import com.appgym.business.repository.MembershipPlanRepository;
import com.appgym.business.web.dto.MembershipPlanRequest;
import com.appgym.business.web.dto.MembershipPlanResponse;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MembershipPlanService {

    private final MembershipPlanRepository repository;

    public MembershipPlanService(MembershipPlanRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public MembershipPlanResponse create(UUID businessId, MembershipPlanRequest request) {
        MembershipPlan plan = new MembershipPlan(
                businessId,
                request.name(),
                request.description(),
                request.priceCents(),
                request.currency(),
                request.durationDays()
        );
        repository.save(plan);
        return MembershipPlanResponse.from(plan);
    }

    public List<MembershipPlanResponse> listByBusiness(UUID businessId) {
        return repository.findByBusinessIdOrderByCreatedAtAsc(businessId).stream()
                .map(MembershipPlanResponse::from)
                .toList();
    }

    @Transactional
    public MembershipPlanResponse update(UUID businessId, UUID planId, MembershipPlanRequest request) {
        MembershipPlan plan = findOwned(businessId, planId);
        plan.setName(request.name());
        plan.setDescription(request.description());
        plan.setPriceCents(request.priceCents());
        plan.setCurrency(request.currency());
        plan.setDurationDays(request.durationDays());
        return MembershipPlanResponse.from(plan);
    }

    @Transactional
    public void delete(UUID businessId, UUID planId) {
        MembershipPlan plan = findOwned(businessId, planId);
        repository.delete(plan);
    }

    private MembershipPlan findOwned(UUID businessId, UUID planId) {
        return repository.findByIdAndBusinessId(planId, businessId)
                .orElseThrow(() -> new NoSuchElementException("Plan no encontrado: " + planId));
    }
}
