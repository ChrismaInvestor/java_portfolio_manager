package com.portfolio.manager.factory;

import com.portfolio.manager.integration.MarketDataClient;
import com.portfolio.manager.integration.OrderPlacementClient;
import com.portfolio.manager.repository.SubOrderRepo;
import com.portfolio.manager.repository.TradeRepo;
import com.portfolio.manager.service.OrderService;
import jakarta.annotation.Resource;

import java.time.LocalDateTime;

public abstract class OrderExecutionFactory {
    @Resource
    OrderService orderService;

    @Resource
    MarketDataClient marketDataClient;

    @Resource
    OrderPlacementClient orderPlacementClient;

    @Resource
    SubOrderRepo subOrderRepo;

    @Resource
    TradeRepo tradeRepo;

    abstract public void executeOrders(String portfolioName);

    boolean isBetween(LocalDateTime startTime, LocalDateTime endTime) {
        LocalDateTime now = LocalDateTime.now();
        return !now.isBefore(startTime) && !now.isAfter(endTime);
    }
}
