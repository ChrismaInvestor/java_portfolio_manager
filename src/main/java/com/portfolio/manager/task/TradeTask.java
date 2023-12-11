package com.portfolio.manager.task;

import com.portfolio.manager.domain.*;
import com.portfolio.manager.dto.BidAskBrokerDTO;
import com.portfolio.manager.dto.PositionIntegrateDTO;
import com.portfolio.manager.integration.MarketDataService;
import com.portfolio.manager.integration.OrderPlacementService;
import com.portfolio.manager.repository.OrderRepo;
import com.portfolio.manager.service.AlgoService;
import com.portfolio.manager.service.OrderService;
import com.portfolio.manager.service.PortfolioService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    MarketDataService marketDataService;

    @Resource
    OrderPlacementService orderPlacementService;

    @Scheduled(fixedDelay = 3000L)
    public void placeOrder() {
        portfolioService.listPortfolio().forEach(portfolioDTO -> {
            List<Order> orders = orderService.listOrders(portfolioDTO.name());
            Set<String> securityCodes = new HashSet<>();
            if (!orders.isEmpty()) {
                orders.forEach(order -> {
                    List<SubOrder> subOrders = order.getSubOrders().stream().filter(subOrder -> this.isBetween(subOrder.getStartTime(), subOrder.getEndTime()) && subOrder.getRemainingShare() > 0).toList();
                    if (!subOrders.isEmpty()) {
                        securityCodes.addAll(subOrders.stream().map(SubOrder::getSecurityCode).collect(Collectors.toSet()));
                    }
                });
                if (!securityCodes.isEmpty()) {
                    Map<String, BidAskBrokerDTO> bidAsks = marketDataService.getBidAsk(securityCodes.stream().toList()).stream().collect(Collectors.toMap(BidAskBrokerDTO::securityCode, Function.identity()));
                    orders.forEach(order -> {
//                        List<SubOrder> subOrders = order.getSubOrders().stream().filter(subOrder -> this.isBetween(subOrder.getStartTime(), subOrder.getEndTime()) && subOrder.getRemainingShare() > 0).toList();
                        List<SubOrder> subOrders = order.getSubOrders().stream().filter(subOrder -> this.isBetween(subOrder.getStartTime(), subOrder.getEndTime()) && subOrder.getRemainingShare() > 0).toList();
                        if (!subOrders.isEmpty()) {
                            subOrders.stream().parallel().forEach(subOrder -> {
                                if (order.getBuyOrSell().equals(Direction.买入)) {
                                    if (bidAsks.get(order.getSecurityCode()).askVol1() >= subOrder.getRemainingShare()) {
                                        algoService.execute(subOrder, order.getId(), bidAsks.get(order.getSecurityCode()).askPrice1(), subOrder.getRemainingShare().intValue());
                                    } else {
                                        algoService.execute(subOrder, order.getId(), bidAsks.get(order.getSecurityCode()).askPrice1(), bidAsks.get(order.getSecurityCode()).askVol1());
                                    }

                                } else if (order.getBuyOrSell().equals(Direction.卖出)) {
                                    if (bidAsks.get(order.getSecurityCode()).bidVol1() >= subOrder.getRemainingShare()) {
                                        algoService.execute(subOrder, order.getId(), bidAsks.get(order.getSecurityCode()).bidPrice1(), subOrder.getRemainingShare().intValue());
                                    } else {
                                        algoService.execute(subOrder, order.getId(), bidAsks.get(order.getSecurityCode()).bidPrice1(), bidAsks.get(order.getSecurityCode()).bidVol1());
                                    }
                                }
                            });
                        }
                    });
                }
            }


        });
    }

    @Scheduled(fixedDelay = 6000L)
    public void updateMainOrder() {
        portfolioService.listPortfolio().forEach(portfolioDTO -> {
            List<Order> orders = orderService.listOrders(portfolioDTO.name());
            Portfolio portfolio = portfolioService.getPortfolio(portfolioDTO.name());
            orders.forEach(order -> {
                PositionIntegrateDTO position = orderPlacementService.checkPosition(order.getSecurityCode());
                if (position.vol()!=null){
                    List<Position> positionList = portfolio.getPositions().stream().filter(p -> p.getSecurityCode().equals(order.getSecurityCode())).toList();
                    if (positionList.isEmpty()){
                        Position currentPosition = new Position();
                        currentPosition.setSecurityCode(order.getSecurityCode());
                        currentPosition.setSecurityShare(Long.valueOf(position.vol()));
                        currentPosition.setCost(BigDecimal.valueOf(position.unitCost()).multiply(BigDecimal.valueOf(currentPosition.getSecurityShare())).doubleValue());
                        portfolioService.updatePosition(currentPosition);
                        portfolio.getPositions().add(currentPosition);
                    }else{
                        Position currentPosition = positionList.get(0);
                        currentPosition.setSecurityShare(Long.valueOf(position.vol()));
                        currentPosition.setCost(BigDecimal.valueOf(position.unitCost()).multiply(BigDecimal.valueOf(currentPosition.getSecurityShare())).doubleValue());
                        portfolioService.updatePosition(currentPosition);
                    }
                }
                long sum = order.getSubOrders().stream().mapToLong(SubOrder::getRemainingShare).sum();
                order.setRemainingShare(sum);
//                if (order.getPlannedShare() - sum > 0) {
//                    Position position = new Position();
//                    position.setSecurityCode(order.getSecurityCode());
//                    position.setSecurityShare(order.getPlannedShare() - sum);
//                    position.setCost(orderService.getCost(order.getId()));
//                    positions.add(position);
//                }
            });
            orderRepo.saveAll(orders);
            portfolioService.updatePortfolio(portfolio);
//            portfolioService.appendPositions(portfolioDTO, positions);
        });
    }

    private boolean isBetween(LocalDateTime startTime, LocalDateTime endTime) {
        LocalDateTime now = LocalDateTime.now();
        return !now.isBefore(startTime) && !now.isAfter(endTime);
    }
}
