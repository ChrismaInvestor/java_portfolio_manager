package com.portfolio.manager.task;

import com.portfolio.manager.domain.Order;
import com.portfolio.manager.domain.Position;
import com.portfolio.manager.domain.SubOrder;
import com.portfolio.manager.dto.BidAskDTO;
import com.portfolio.manager.dto.OrderInProgressDTO;
import com.portfolio.manager.integration.BidAskService;
import com.portfolio.manager.repository.OrderRepo;
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
public class TradeTask {

    @Resource
    OrderService orderService;

    @Resource
    OrderRepo orderRepo;

    @Resource
    PortfolioService portfolioService;

    @Resource
    AlgoService algoService;

    @Scheduled(fixedDelay = 3000L)
    public void placeOrder() {
        portfolioService.listPortfolio().forEach(portfolioDTO -> {
            List<Order> orders = orderService.listOrders(portfolioDTO.name());
//            List<SubOrder> subOrders = new ArrayList<>();
            orders.forEach(order -> order.getSubOrders().stream().filter(subOrder -> this.isBetween(subOrder.getStartTime(), subOrder.getEndTime()) && subOrder.getRemainingShare() > 0).parallel().forEach(
                    subOrder -> algoService.execute(subOrder, order.getId()))
//                    subOrders.addAll(order.getSubOrders().stream().filter(subOrder -> this.isBetween(subOrder.getStartTime(), subOrder.getEndTime()) && subOrder.getRemainingShare() > 0).toList()));
//            subOrders.stream().parallel().forEach(subOrder -> algoService.execute(subOrder));
            );
        });
    }

    @Scheduled(fixedDelay = 6000L)
    public void updateMainOrder() {
        portfolioService.listPortfolio().forEach(portfolioDTO -> {
            List<Order> orders = orderService.listOrders(portfolioDTO.name());
            List<Position> positions = new ArrayList<>();
            orders.forEach(order -> {
                long sum = order.getSubOrders().stream().mapToLong(SubOrder::getRemainingShare).sum();
                order.setRemainingShare(sum);
                if (order.getPlannedShare() - sum > 0) {
                    Position position = new Position();
                    position.setSecurityCode(order.getSecurityCode());
                    position.setSecurityShare(order.getPlannedShare() - sum);
                    position.setCost(orderService.getCost(order.getId()));
                    positions.add(position);
                }
            });
            orderRepo.saveAll(orders);
            portfolioService.appendPositions(portfolioDTO, positions);
        });
    }

    private boolean isBetween(LocalDateTime startTime, LocalDateTime endTime) {
        LocalDateTime now = LocalDateTime.now();
        return !now.isBefore(startTime) && !now.isAfter(endTime);
    }
}
