package com.appgym.business.service;

import com.appgym.business.domain.Business;
import com.appgym.business.repository.BusinessRepository;
import com.appgym.business.web.dto.BusinessResponse;
import com.appgym.business.web.dto.CreateBusinessRequest;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BusinessService {

    private final BusinessRepository repository;

    public BusinessService(BusinessRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public BusinessResponse create(CreateBusinessRequest request) {
        Business business = new Business(
                UUID.randomUUID(),
                request.name(),
                request.type(),
                request.description(),
                request.contactEmail(),
                request.contactPhone(),
                request.address(),
                request.primaryColor()
        );
        repository.save(business);
        return BusinessResponse.from(business);
    }

    public List<BusinessResponse> listAll() {
        return repository.findAll().stream().map(BusinessResponse::from).toList();
    }

    public BusinessResponse getById(UUID id) {
        return repository.findById(id)
                .map(BusinessResponse::from)
                .orElseThrow(() -> new NoSuchElementException("Negocio no encontrado: " + id));
    }
}
