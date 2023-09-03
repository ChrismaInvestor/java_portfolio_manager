package com.portfolio.manager.service;

import com.portfolio.manager.domain.Position;
import com.portfolio.manager.dto.OrderDTO;
import com.portfolio.manager.repository.DynamicsRepo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Service
public class PositionServiceImpl implements PositionService {
    @Resource
    private DynamicsRepo dynamicsRepo;

    @Resource
    private SecurityService securityService;

    @Resource
    private PriceService priceService;

    @Override
    public List<Position> listPosition(String portfolioName) {
        return dynamicsRepo.findByPortfolioName(portfolioName).getPositions();
    }

    @Override
    public Double getCash(String portfolioName) {
        return dynamicsRepo.findByPortfolioName(portfolioName).getCash();
    }

    @Override
    public List<OrderDTO> buySplitEven(Set<String> securityCodes, double toSellMarketValue, double cash) {
        // Not developed yet
        return Collections.emptyList();
    }


    @Override
    public List<OrderDTO> sell(List<Position> toSell) {
        return toSell.stream().map(position -> new OrderDTO("卖", position.getSecurityShare(), securityService.getSecurityName(position.getSecurityCode()), position.getSecurityCode(), BigDecimal.valueOf(priceService.getLatestPrice(position.getSecurityCode()).getPrice()).multiply(BigDecimal.valueOf(position.getSecurityShare())).doubleValue())).toList();
    }


}
