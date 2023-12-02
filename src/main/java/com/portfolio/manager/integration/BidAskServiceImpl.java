package com.portfolio.manager.integration;

import com.portfolio.manager.dto.BidAskBrokerDTO;
import com.portfolio.manager.dto.BidAskDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
public class BidAskServiceImpl implements BidAskService {
    @Resource
    RestTemplate restTemplate;

    @Override
    public BidAskDTO getSell1(String securityCode) {
        ResponseEntity<BidAskDTO> bidAsk;
        try {
            bidAsk = restTemplate.exchange("http://localhost:5000/bidAsk/buy/{code}", HttpMethod.GET, null, BidAskDTO.class, securityCode);
            return bidAsk.getBody();
        } catch (Exception e) {
            log.error("Bid ask query: {}", e.getMessage());
        }
        return null;
    }

    @Override
    public BidAskDTO getBuy1(String securityCode) {
        ResponseEntity<BidAskDTO> bidAsk;
        try {
            bidAsk = restTemplate.exchange("http://localhost:5000/bidAsk/sell/{code}", HttpMethod.GET, null, BidAskDTO.class, securityCode);
            return bidAsk.getBody();
        } catch (Exception e) {
            log.error("Bid ask query: {}", e.getMessage());
        }
        return null;
    }

    @Override
    public List<BidAskBrokerDTO> getBidAsk(List<String> securityCodes) {
        ResponseEntity<List<Map>> bidAsk;
        try {
            bidAsk = restTemplate.exchange("http://localhost:5000/bidAsk/{codes}", HttpMethod.GET, null, new ParameterizedTypeReference<List<Map>>() {
            }, String.join(",", securityCodes));
            return Objects.requireNonNull(bidAsk.getBody()).stream().map(v->new BidAskBrokerDTO(v.get("securityCode").toString(), Double.parseDouble(v.get("askPrice1").toString()),Double.parseDouble(v.get("bidPrice1").toString()),Integer.parseInt(v.get("askVol1").toString()),Integer.parseInt(v.get("bidVol1").toString()))).toList();
        } catch (Exception e) {
            log.error("Bid ask query: {}", e.getMessage());
        }
        return null;
    }
}
