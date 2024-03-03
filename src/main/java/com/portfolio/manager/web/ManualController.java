package com.portfolio.manager.web;

import com.portfolio.manager.domain.Dynamics;
import com.portfolio.manager.domain.Portfolio;
import com.portfolio.manager.dto.OrderDTO;
import com.portfolio.manager.service.OrderService;
import com.portfolio.manager.service.PortfolioService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

record SecurityAndWeight(String securityCode, BigDecimal weight){}

record ManualInput(String portfolioName, List<SecurityAndWeight> securityAndWeights){}

@Slf4j
@RestController
@RequestMapping("/manual")
@CrossOrigin
public class ManualController {
    @Resource
    OrderService orderService;

    @Resource
    PortfolioService portfolioService;

    @PostMapping("cal")
    public List<OrderDTO> calOrders(@RequestBody ManualInput manualInput){
        Portfolio portfolio = portfolioService.getPortfolio(manualInput.portfolioName());
        Dynamics dynamics = portfolioService.getDynamics(portfolio);
        Map<String, BigDecimal> map = manualInput.securityAndWeights().stream().collect(Collectors.toMap(SecurityAndWeight::securityCode, SecurityAndWeight::weight));
        List<OrderDTO> orders= orderService.generateOrder(map, BigDecimal.valueOf(dynamics.getCash()));
        log.info("orders: {}", orders);
        return orders;
    }
}
