package com.portfolio.manager.integration;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class OrderPlacementServiceImpl implements OrderPlacementService{
    @Resource
    RestTemplate restTemplate;

    @Value("${host.ip}")
    private String hostIP;

    @Override
    public String buy(String code, Double price, Integer vol) {
        ResponseEntity<String> orderId;
        try {
            orderId = restTemplate.exchange("http://" + hostIP + "/buy/{code}/{price}/{vol}", HttpMethod.GET, null, new ParameterizedTypeReference<>() {
            }, code, price, vol);
            return orderId.getBody();
        } catch (Exception e) {
            log.error("Buy query: {}", e.getMessage());
        }
        return null;
    }

    @Override
    public String sell(String code, Double price, Integer vol) {
        ResponseEntity<String> orderId;
        try {
            orderId = restTemplate.exchange("http://" + hostIP + "/sell/{code}/{price}/{vol}", HttpMethod.GET, null, new ParameterizedTypeReference<>() {
            }, code, price, vol);
            return orderId.getBody();
        } catch (Exception e) {
            log.error("Buy query: {}", e.getMessage());
        }
        return null;
    }
}
