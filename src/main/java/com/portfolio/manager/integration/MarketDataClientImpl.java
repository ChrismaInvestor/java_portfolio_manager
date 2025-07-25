package com.portfolio.manager.integration;

import com.portfolio.manager.domain.Price;
import com.portfolio.manager.domain.strategy_specific.CbStockMapping;
import com.portfolio.manager.dto.integration.BidAskBrokerDTO;
import com.portfolio.manager.dto.integration.SecurityInfoDTO;
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
    public List<SecurityInfoDTO> listAllStocksInfo() {
        ResponseEntity<List<SecurityInfoDTO>> stocks;
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
    public List<CbStockMapping> listCbStockMapping() {
        ResponseEntity<List<CbStockMapping>> cbStockMappings;
        try {
            cbStockMappings = restTemplate.exchange("http://" + hostIP + "/cbStockMapping", HttpMethod.GET, null, new ParameterizedTypeReference<>() {
            });
            return cbStockMappings.getBody();
        } catch (Exception e) {
            log.error("Cb Stock Mapping query: {}", e.getMessage());
        }
        return null;
    }

    @Override
    public List<BidAskBrokerDTO> getBidAsk(List<String> securityCodes) {
        ResponseEntity<List<Map>> bidAsk;
        if (securityCodes.isEmpty()){
            throw new IllegalArgumentException("empty codes");
        }
        try {
            bidAsk = restTemplate.exchange("http://" + hostIP + "/bidAsk/{codes}", HttpMethod.GET, null, new ParameterizedTypeReference<>() {
            }, String.join(",", securityCodes));

            return Objects.requireNonNull(bidAsk.getBody()).stream().map(v -> new BidAskBrokerDTO(v.get("securityCode").toString(), Double.parseDouble(v.get("askPrice1").toString()), Double.parseDouble(v.get("bidPrice1").toString()), Integer.parseInt(v.get("askVol1").toString()), Integer.parseInt(v.get("bidVol1").toString()), Double.parseDouble(v.get("lastPrice").toString()), Double.parseDouble(v.get("lastClose").toString()),
                    Integer.parseInt(v.get("askVol2").toString()), Double.parseDouble(v.get("askPrice2").toString()), Integer.parseInt(v.get("bidVol2").toString()), Double.parseDouble(v.get("bidPrice2").toString()), Double.parseDouble(v.get("high").toString()))).toList();
        } catch (Exception e) {
            log.error("Bid ask query: {}", e.getMessage());
        }
        return null;
    }

    @Override
    public List<Price> listMinPrice(List<String> securityCodes) {
        ResponseEntity<List<Price>> minPrices;
        try {
            minPrices = restTemplate.exchange("http://" + hostIP + "/minPrice/{codes}", HttpMethod.GET, null, new ParameterizedTypeReference<>() {
            }, String.join(",", securityCodes));
            return minPrices.getBody();
        } catch (Exception e) {
            log.error("MinPrice query: {}", e.getMessage());
        }
        return null;
    }
}
