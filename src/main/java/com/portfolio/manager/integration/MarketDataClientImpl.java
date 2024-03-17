package com.portfolio.manager.integration;

import com.portfolio.manager.domain.Price;
import com.portfolio.manager.dto.BidAskBrokerDTO;
import com.portfolio.manager.dto.SecurityDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Slf4j
@Service
public class MarketDataClientImpl implements MarketDataClient {
    @Resource
    RestTemplate restTemplate;

    @Value("${host.ip}")
    private String hostIP;

    @Override
    public List<SecurityDTO> listAllStocksInfo() {
        ResponseEntity<List<SecurityDTO>> stocks;
        try {
            stocks = restTemplate.exchange("http://" + hostIP + "/stocks", HttpMethod.GET, null, new ParameterizedTypeReference<>() {
            });
            return stocks.getBody();
        } catch (Exception e) {
            log.error("Stocks query: {}", e.getMessage());
        }
        return null;
    }

    @Override
    public List<BidAskBrokerDTO> getBidAsk(List<String> securityCodes) {
        ResponseEntity<List<Map>> bidAsk;
        try {
            bidAsk = restTemplate.exchange("http://" + hostIP + "/bidAsk/{codes}", HttpMethod.GET, null, new ParameterizedTypeReference<>() {
            }, String.join(",", securityCodes));
            return Objects.requireNonNull(bidAsk.getBody()).stream().map(v -> new BidAskBrokerDTO(v.get("securityCode").toString(), Double.parseDouble(v.get("askPrice1").toString()), Double.parseDouble(v.get("bidPrice1").toString()), Integer.parseInt(v.get("askVol1").toString()), Integer.parseInt(v.get("bidVol1").toString()), Double.parseDouble(v.get("lastPrice").toString()), Double.parseDouble(v.get("lastClose").toString()),
                    Integer.parseInt(v.get("askVol2").toString()), Double.parseDouble(v.get("askPrice2").toString()),Integer.parseInt(v.get("bidVol2").toString()), Double.parseDouble(v.get("bidPrice2").toString()))).toList();
        } catch (Exception e) {
            log.error("Bid ask query: {}", e.getMessage());
        }
        return null;
    }

    @Override
    public List<Price> listMinPrice(List<String> securityCodes) {
        ResponseEntity<String> minPrices;
        try {
            minPrices = restTemplate.exchange("http://" + hostIP + "/minPrice/{code}", HttpMethod.GET, null, new ParameterizedTypeReference<>() {
            }, String.join(",", securityCodes));
            return Collections.emptyList();
//            return minPrices.getBody();
        } catch (Exception e) {
            log.error("MinPrice query: {}", e.getMessage());
        }
        return null;
    }
}
