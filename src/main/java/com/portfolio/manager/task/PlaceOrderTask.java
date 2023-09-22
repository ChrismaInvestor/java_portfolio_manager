package com.portfolio.manager.task;

import com.portfolio.manager.domain.Order;
import com.portfolio.manager.dto.BidAskDTO;
import com.portfolio.manager.dto.OrderInProgressDTO;
import com.portfolio.manager.integration.BidAskService;
import com.portfolio.manager.service.OrderService;
import com.portfolio.manager.service.PortfolioService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

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

    @Scheduled(fixedDelay = 3000L)
    public void placeOrder(){
portfolioService.listPortfolio().forEach(portfolioDTO -> {
    List<Order> orders = orderService.listOrders(portfolioDTO.name());
});
//        BidAskDTO bidAskDTO = bidAskService.getSell1(code);
    }
}
