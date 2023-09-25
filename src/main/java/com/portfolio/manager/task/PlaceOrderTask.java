package com.portfolio.manager.task;

import com.portfolio.manager.domain.Order;
import com.portfolio.manager.domain.SubOrder;
import com.portfolio.manager.dto.BidAskDTO;
import com.portfolio.manager.dto.OrderInProgressDTO;
import com.portfolio.manager.integration.BidAskService;
import com.portfolio.manager.service.AlgoService;
import com.portfolio.manager.service.OrderService;
import com.portfolio.manager.service.PortfolioService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class PlaceOrderTask {
    @Resource
    BidAskService bidAskService;

    @Resource
    OrderService orderService;

    @Resource
    PortfolioService portfolioService;

    @Resource
    AlgoService algoService;

    @Scheduled(fixedDelay = 3000L)
    public void placeOrder() {
        portfolioService.listPortfolio().forEach(portfolioDTO -> {
            List<Order> orders = orderService.listOrders(portfolioDTO.name());
            List<SubOrder> subOrders = new ArrayList<>();
            orders.forEach(order -> subOrders.addAll(order.getSubOrders().stream().filter(subOrder -> this.isBetween(subOrder.getStartTime(), subOrder.getEndTime())).toList()));
            subOrders.stream().parallel().forEach(subOrder -> algoService.execute(subOrder));
        });
    }

    private boolean isBetween(LocalDateTime startTime, LocalDateTime endTime) {
        LocalDateTime now = LocalDateTime.now();
        return !now.isBefore(startTime) && !now.isAfter(endTime);
    }
}
