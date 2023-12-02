package com.portfolio.manager.task;

import com.portfolio.manager.domain.Direction;
import com.portfolio.manager.domain.Order;
import com.portfolio.manager.domain.Position;
import com.portfolio.manager.domain.SubOrder;
import com.portfolio.manager.dto.BidAskBrokerDTO;
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
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    @Resource
    BidAskService bidAskService;

    @Scheduled(fixedDelay = 3000L)
    public void placeOrder() {
        portfolioService.listPortfolio().forEach(portfolioDTO -> {
            List<Order> orders = orderService.listOrders(portfolioDTO.name());
            Set<String> securityCodes = new HashSet<>();
            if (!orders.isEmpty()) {
                orders.forEach(order -> {
                    List<SubOrder> subOrders = order.getSubOrders().stream().filter(subOrder -> subOrder.getRemainingShare() > 0).toList();
                    if (!subOrders.isEmpty()) {
                        securityCodes.addAll(subOrders.stream().map(SubOrder::getSecurityCode).collect(Collectors.toSet()));
                    }
                });
                Map<String, BidAskBrokerDTO> bidAsks = bidAskService.getBidAsk(securityCodes.stream().toList()).stream().collect(Collectors.toMap(BidAskBrokerDTO::securityCode, Function.identity()));
                orders.stream().filter(order -> bidAsks.get(order.getSecurityCode()).askVol1() > 0 || bidAsks.get(order.getSecurityCode()).bidVol1() > 0).forEach(order -> {
//                        List<SubOrder> subOrders = order.getSubOrders().stream().filter(subOrder -> this.isBetween(subOrder.getStartTime(), subOrder.getEndTime()) && subOrder.getRemainingShare() > 0).toList();
                    List<SubOrder> subOrders = order.getSubOrders().stream().filter(subOrder -> subOrder.getRemainingShare() > 0).toList();
                    if (!subOrders.isEmpty()) {
                        subOrders.stream().parallel().forEach(subOrder -> {
                            if (order.getBuyOrSell().equals(Direction.买入)) {
                                algoService.execute(subOrder, order.getId(), bidAsks.get(order.getSecurityCode()).askPrice1(), bidAsks.get(order.getSecurityCode()).askVol1());
                            } else if (order.getBuyOrSell().equals(Direction.卖出)) {
                                algoService.execute(subOrder, order.getId(), bidAsks.get(order.getSecurityCode()).bidPrice1(), bidAsks.get(order.getSecurityCode()).bidVol1());
                            }
                        });
                    }
                });
            }


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
