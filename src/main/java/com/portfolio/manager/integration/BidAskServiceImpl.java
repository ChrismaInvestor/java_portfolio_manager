package com.portfolio.manager.integration;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class BidAskServiceImpl implements BidAskService{
    @Resource
    RestTemplate restTemplate;

    @Override
    public String getBidAskInfo(String securityCode) {
        ResponseEntity<String> bidAsk;
        try{
            bidAsk = restTemplate.exchange("http://localhost:5000/bidAsk/{code}", HttpMethod.GET, null,String.class, securityCode);
            return bidAsk.getBody();

        }catch (Exception e){
            log.error("Bid ask query: {}", e.getMessage());
        }
        return null;
    }
}
