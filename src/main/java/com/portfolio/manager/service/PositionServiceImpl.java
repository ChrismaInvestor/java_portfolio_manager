package com.portfolio.manager.service;

import com.portfolio.manager.domain.Position;
import com.portfolio.manager.dto.OrderDTO;
import com.portfolio.manager.repository.DynamicsRepo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
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
    public List<OrderDTO> buySplitEven(Set<String> securityCodes, double toSellMarketValue, double cash, List<Position> holdings) {
        BigDecimal totalValue = BigDecimal.valueOf(toSellMarketValue).add(BigDecimal.valueOf(cash));
        final BigDecimal average = totalValue.divide(BigDecimal.valueOf(securityCodes.size()), RoundingMode.HALF_DOWN).setScale(2, RoundingMode.HALF_DOWN);
        List<BigDecimal> maxTotal = new ArrayList<>();
        List<BigDecimal> minTotal = new ArrayList<>();
        List<OrderDTO> orders = new ArrayList<>();
        Map<String, Position> holdingCodes = holdings.stream().collect(Collectors.toMap(Position::getSecurityCode, Function.identity()));
        securityCodes.forEach(code -> {
            String internalCode = code.split("\\.")[0];
            BigDecimal price = BigDecimal.valueOf(priceService.getLatestPrice(internalCode).getPrice());
            BigDecimal divide = average.divide(price.multiply(BigDecimal.valueOf(100L)), RoundingMode.HALF_EVEN);
            BigDecimal min = divide.setScale(0, RoundingMode.DOWN);
            BigDecimal max = divide.setScale(0, RoundingMode.UP);
            maxTotal.add(max.multiply(price).multiply(BigDecimal.valueOf(100L)));
            minTotal.add(min.multiply(price).multiply(BigDecimal.valueOf(100L)));
            if (holdingCodes.containsKey(internalCode)) {
                min = min.subtract(BigDecimal.valueOf(holdingCodes.get(internalCode).getSecurityShare()));
                if (min.compareTo(BigDecimal.ZERO) < 0) {
                    OrderDTO order = new OrderDTO("卖出", min.abs().multiply(BigDecimal.valueOf(100L)).longValue(), securityService.getSecurityName(code.split("\\.")[0]), code, min.abs().multiply(price).multiply(BigDecimal.valueOf(100L)).doubleValue());
                    orders.add(order);
                } else {
                    OrderDTO order = new OrderDTO("买入", min.multiply(BigDecimal.valueOf(100L)).longValue(), securityService.getSecurityName(code.split("\\.")[0]), code, min.multiply(price).multiply(BigDecimal.valueOf(100L)).doubleValue());
                    orders.add(order);
                }

            } else {
                OrderDTO order = new OrderDTO("买入", min.multiply(BigDecimal.valueOf(100L)).longValue(), securityService.getSecurityName(code.split("\\.")[0]), code, min.multiply(price).multiply(BigDecimal.valueOf(100L)).doubleValue());
                orders.add(order);
            }
        });
        log.info("Max total: {}, min total: {}", maxTotal.stream().mapToDouble(BigDecimal::doubleValue).sum(), minTotal.stream().mapToDouble(BigDecimal::doubleValue).sum());
        return orders;
    }


    @Override
    public List<OrderDTO> sell(List<Position> toSell) {
        return toSell.stream().map(position -> new OrderDTO("卖", position.getSecurityShare(), securityService.getSecurityName(position.getSecurityCode()), position.getSecurityCode(), BigDecimal.valueOf(priceService.getLatestPrice(position.getSecurityCode()).getPrice()).multiply(BigDecimal.valueOf(position.getSecurityShare())).doubleValue())).toList();
    }


}
