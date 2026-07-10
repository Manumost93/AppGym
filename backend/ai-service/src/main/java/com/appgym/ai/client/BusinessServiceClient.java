package com.appgym.ai.client;

import com.appgym.ai.config.InternalServicesProperties;
import com.appgym.common.security.JwtClaims;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class BusinessServiceClient {

    private final RestClient restClient;

    public BusinessServiceClient(InternalServicesProperties properties) {
        this.restClient = RestClient.builder().baseUrl(properties.businessServiceUrl()).build();
    }

    public Optional<BusinessInfo> findByBusinessId(UUID businessId) {
        try {
            BusinessInfo info = restClient.get()
                    .uri("/api/business/me")
                    .header(JwtClaims.HEADER_BUSINESS_ID, businessId.toString())
                    .retrieve()
                    .body(BusinessInfo.class);
            return Optional.ofNullable(info);
        } catch (RestClientException e) {
            return Optional.empty();
        }
    }
}
