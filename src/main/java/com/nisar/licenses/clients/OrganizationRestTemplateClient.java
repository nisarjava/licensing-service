package com.nisar.licenses.clients;

import com.nisar.licenses.model.Organization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.function.Function;
import java.util.function.Supplier;

@Component
public class OrganizationRestTemplateClient {
    @Autowired
    RestTemplate restTemplate;

    public Organization getOrganization(String organizationId){
       
        ResponseEntity<Organization> restExchange =
                restTemplate.exchange(
                        "http://ORGANISATIONSERVICE/v1/organizations/{organizationId}",
                        HttpMethod.GET,
                        null, Organization.class, organizationId);

        return restExchange.getBody();
    }
}
