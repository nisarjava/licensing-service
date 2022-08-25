package com.nisar.licenses.services;

import com.nisar.licenses.clients.OrganizationDiscoveryClient;
import com.nisar.licenses.clients.OrganizationFeignClient;
import com.nisar.licenses.clients.OrganizationRestTemplateClient;
import com.nisar.licenses.config.ServiceConfig;
import com.nisar.licenses.domain.License;
import com.nisar.licenses.model.Organization;
import com.nisar.licenses.repository.LicenseRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class LicenseService {
    @Autowired
    private LicenseRepository licenseRepository;
    @Autowired
    ServiceConfig config;

    @Autowired
    OrganizationFeignClient organizationFeignClient;

    @Autowired
    OrganizationRestTemplateClient organizationRestClient;

    @Autowired
    OrganizationDiscoveryClient organizationDiscoveryClient;

    public License getLicense(String organizationId, String licenseId) {
        License license = licenseRepository.findByOrganizationIdAndLicenseId(
                organizationId, licenseId);
        return license.withComment(config.getExampleProperty());
    }


  // @CircuitBreaker(name = "getLicensesByOrg", fallbackMethod = "getCacheLicenses")
    int i=1;
  //  @Retry( name = "getLicensesByOrg" ,fallbackMethod ="getCacheLicenses" )
    @RateLimiter(name = "getLicensesByOrg" ,fallbackMethod ="getCacheLicenses"  )
    public List<License> getLicensesByOrg(String organizationId){
       // randomlyRunLong();
        System.out.println("Calling ORG "+i);
       i++;
        Organization org = retrieveOrgInfo(organizationId, "blank");

       System.out.println(org);
     return     licenseRepository.findByOrganizationId( organizationId );

    }
    public List<License> getCacheLicenses(String s, Throwable t ){

        License l= new License().withOrganizationName( "TEST")
                 .withContactName("TESTContact")
                 .withContactEmail( "TESTEmail" )
                 .withContactPhone( "TESTPhone" )
                 .withComment(config.getExampleProperty());
        System.out.println("I am returning new L");
        System.out.println(l);
       return Arrays.asList(l);
    }
    private void randomlyRunLong(){
       // Random rand = new Random();
        //int randomNum = rand.nextInt((3 - 1) + 1) + 1;
        //if (randomNum==3)
        sleep();
    }
    private void sleep(){
        try {
            Thread.sleep(22000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void saveLicense(License license){
        license.withId( UUID.randomUUID().toString());
        licenseRepository.save(license);
    }

    public License getLicense(String organizationId, String licenseId, String
            clientType) {
        License license = licenseRepository.findByOrganizationIdAndLicenseId(organizationId, licenseId);
        Organization org = retrieveOrgInfo(organizationId, clientType);
        return license
                .withOrganizationName( org.getName())
                .withContactName( org.getContactName())
                .withContactEmail( org.getContactEmail() )
                .withContactPhone( org.getContactPhone() )
                .withComment(config.getExampleProperty());
    }
    private Organization retrieveOrgInfo(String organizationId, String clientType){
        Organization organization = null;

        switch (clientType) {
            case "feign":
                System.out.println("I am using the feign client");
                organization = organizationFeignClient.getOrganization(organizationId);
                break;
            case "rest":
                System.out.println("I am using the rest client");
                organization = organizationRestClient.getOrganization(organizationId);
                break;
            case "discovery":
                System.out.println("I am using the discovery client");
                organization = organizationDiscoveryClient.getOrganization(organizationId);
                break;
            default:
                organization = organizationRestClient.getOrganization(organizationId);
        }

        return organization;
    }
}
