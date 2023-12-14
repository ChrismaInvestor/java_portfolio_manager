package com.portfolio.manager.integration;

import com.portfolio.manager.dto.PositionIntegrateDTO;
import com.portfolio.manager.dto.TradeDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
@Service
public class OrderPlacementServiceImpl implements OrderPlacementService {
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

    @Override
    public PositionIntegrateDTO checkPosition(String code) {
        ResponseEntity<PositionIntegrateDTO> res;
        try {
            res = restTemplate.exchange("http://" + hostIP + "/position/{code}", HttpMethod.GET, null, new ParameterizedTypeReference<>() {
            }, code);
            return res.getBody();
        } catch (Exception e) {
            log.error("Code: {}, Check position: {}", code, e.getMessage());
        }
        return null;
    }

    @Override
    public List<TradeDTO> listTodayTrades() {
        ResponseEntity<List<TradeDTO>> res;
        try {
            res = restTemplate.exchange("http://" + hostIP + "/todayTrades", HttpMethod.GET, null, new ParameterizedTypeReference<>() {
            });
            return res.getBody();
        } catch (Exception e) {
            log.error("List today trades: {}", e.getMessage());
        }
        return null;
    }
}
