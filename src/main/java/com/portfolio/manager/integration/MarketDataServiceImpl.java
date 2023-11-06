package com.portfolio.manager.integration;

import com.portfolio.manager.domain.Price;
import com.portfolio.manager.dto.SecurityDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
@Service
public class MarketDataServiceImpl implements MarketDataService {
    @Resource
    RestTemplate restTemplate;

    @Override
    public List<SecurityDTO> listAllStocksInfo() {
        ResponseEntity<List<SecurityDTO>> stocks;
        try {
            stocks = restTemplate.exchange("http://localhost:5000/stocks", HttpMethod.GET, null, new ParameterizedTypeReference<>() {
            });
            return stocks.getBody();
        } catch (Exception e) {
            log.error("Stocks query: {}", e.getMessage());
        }
        return null;
    }

    @Override
    public List<Price> listMinPrice(String code) {
        ResponseEntity<List<Price>> minPrices;
        try {
            minPrices = restTemplate.exchange("http://localhost:5000/minPrice/{code}", HttpMethod.GET, null, new ParameterizedTypeReference<>() {
            }, code);
            return minPrices.getBody();
        } catch (Exception e) {
            log.error("MinPrice query: {}", e.getMessage());
        }
        return null;
    }
}
