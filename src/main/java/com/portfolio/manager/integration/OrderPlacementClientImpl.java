package com.portfolio.manager.integration;

import com.portfolio.manager.dto.integration.AccountDTO;
import com.portfolio.manager.dto.integration.CancelableOrderDTO;
import com.portfolio.manager.dto.integration.PositionBrokerDTO;
import com.portfolio.manager.dto.integration.TradeDTO;
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
public class OrderPlacementClientImpl implements OrderPlacementClient {
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
            log.info("code: {}, orderId: {}", code, orderId.getBody());
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
            log.info("code: {}, orderId: {}", code, orderId.getBody());
            return orderId.getBody();
        } catch (Exception e) {
            log.error("Buy query: {}", e.getMessage());
        }
        return null;
    }

//    @Override
//    public PositionIntegrateDTO checkPosition(String code) {
//        ResponseEntity<PositionIntegrateDTO> res;
//        try {
//            res = restTemplate.exchange("http://" + hostIP + "/position/{code}", HttpMethod.GET, null, new ParameterizedTypeReference<>() {
//            }, code);
//            return res.getBody();
//        } catch (Exception e) {
//            log.error("Code: {}, Check position: {}", code, e.getMessage());
//        }
//        return null;
//    }

    @Override
    public List<PositionBrokerDTO> queryAllPositions() {
        ResponseEntity<List<PositionBrokerDTO>> res;
        try {
            res = restTemplate.exchange("http://" + hostIP + "/position", HttpMethod.GET, null, new ParameterizedTypeReference<>() {
            });
            return res.getBody();
        } catch (Exception e) {
            log.error("Check position: {}", e.getMessage());
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

    @Override
    public AccountDTO queryAcct() {
        ResponseEntity<AccountDTO> res;
        try {
            res = restTemplate.exchange("http://" + hostIP + "/asset/cash", HttpMethod.GET, null, new ParameterizedTypeReference<>() {
            });
            return res.getBody();
        } catch (Exception e) {
            log.error("asset cash: {}", e.getMessage());
        }
        return null;
    }

    @Override
    public List<CancelableOrderDTO> queryCancelableOrders() {
        ResponseEntity<List<CancelableOrderDTO>> res;
        try {
            res = restTemplate.exchange("http://" + hostIP + "/order/cancelable", HttpMethod.GET, null, new ParameterizedTypeReference<>() {
            });
            return res.getBody();
        } catch (Exception e) {
            log.error("cancelable orders: {}", e.getMessage());
        }
        return null;
    }

    @Override
    public Boolean cancelOrder(Long orderId) {
        ResponseEntity<String> actionId;
        try {
            actionId = restTemplate.exchange("http://" + hostIP + "/order/cancel/{orderId}", HttpMethod.GET, null, new ParameterizedTypeReference<>() {
            }, orderId);
            if (actionId.getBody() != null) {
                return Long.parseLong(actionId.getBody()) >= 0;
            }
        } catch (Exception e) {
            log.error("Buy query: {}", e.getMessage());
        }
        return null;
    }
}
