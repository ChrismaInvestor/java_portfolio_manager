package com.portfolio.manager.integration;

import com.portfolio.manager.dto.BidAskDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class BidAskServiceImpl implements BidAskService {
    @Resource
    RestTemplate restTemplate;

    @Override
    public BidAskDTO getSell1(String securityCode) {
        ResponseEntity<BidAskDTO> bidAsk;
        try {
            bidAsk = restTemplate.exchange("http://localhost:5000/bidAsk/{code}", HttpMethod.GET, null, BidAskDTO.class, securityCode);
            return bidAsk.getBody();
        } catch (Exception e) {
            log.error("Bid ask query: {}", e.getMessage());
        }
        return null;
    }
}
