package com.portfolio.manager.task;

import com.portfolio.manager.domain.*;
import com.portfolio.manager.domain.strategy_specific.PositionBookForCrown;
import com.portfolio.manager.dto.BidAskBrokerDTO;
import com.portfolio.manager.dto.OrderDTO;
import com.portfolio.manager.dto.PositionIntegrateDTO;
import com.portfolio.manager.dto.TradeDTO;
import com.portfolio.manager.integration.MarketDataService;
import com.portfolio.manager.integration.OrderPlacementService;
import com.portfolio.manager.repository.OrderRepo;
import com.portfolio.manager.repository.PositionBookForCrownRepo;
import com.portfolio.manager.service.AlgoService;
import com.portfolio.manager.service.OrderService;
import com.portfolio.manager.service.PortfolioService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
    MarketDataService marketDataService;

    @Resource
    OrderPlacementService orderPlacementService;

    @Resource
    PositionBookForCrownRepo positionBookForCrownRepo;

    BigDecimal takeProfit = new BigDecimal("1.075");

    BigDecimal stopLoss = new BigDecimal("0.975");

    @Scheduled(fixedDelay = 3000L)
    public void placeOrder() {
        portfolioService.listPortfolioDTO().forEach(portfolioDTO -> {
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

    @Scheduled(cron = "0 50 14 ? * MON-FRI")
    public void buyBackForCrown() {
        portfolioService.listPortfolio().stream().filter(Portfolio::getTakeProfitStopLoss
        ).toList().forEach(portfolio -> {
            List<PositionBookForCrown> positionBook = positionBookForCrownRepo.findByPortfolioName(portfolio.getName());
            Map<String, Position> position = portfolioService.listPosition(portfolio.getName()).stream().collect(Collectors.toMap(Position::getSecurityCode, Function.identity()));
            positionBook.stream().parallel().forEach(positionBookForCrown -> {
                if (position.get(positionBookForCrown.getSecurityCode()) == null) {
                    log.warn("Buy back hit: {}", positionBookForCrown);
                    OrderDTO orderDTO = new OrderDTO(Direction.买入, positionBookForCrown.getSecurityShare(), positionBookForCrown.getSecurityName(), positionBookForCrown.getSecurityCode(), 0.0d);
                    orderService.addOrder(orderDTO, portfolio.getName(), LocalDateTime.now(), LocalDateTime.now().plusMinutes(5L));
                } else if (position.get(positionBookForCrown.getSecurityCode()).getSecurityShare().compareTo(positionBookForCrown.getSecurityShare()) < 0) {
                    log.warn("Buy back hit: {}, difference: {}", positionBookForCrown, positionBookForCrown.getSecurityShare() - position.get(positionBookForCrown.getSecurityCode()).getSecurityShare());
                }
            });

        });
    }

    @Scheduled(fixedDelay = 6000L)
    public void autoTrade() {
        if (!isTradeTime()) {
            return;
        }
        portfolioService.listPortfolio().stream().filter(Portfolio::getTakeProfitStopLoss
        ).toList().stream().parallel().forEach(portfolio -> {
            List<Position> positions = portfolioService.listPosition(portfolio.getName());
            List<String> codes = positions.stream().map(Position::getSecurityCode).toList();
            if (!codes.isEmpty()) {
                marketDataService.getBidAsk(codes).forEach(
                        bidAskBrokerDTO -> {
                            if (BigDecimal.valueOf(bidAskBrokerDTO.bidPrice1()).divide(BigDecimal.valueOf(bidAskBrokerDTO.lastClose()), 4, RoundingMode.HALF_EVEN).compareTo(takeProfit) >= 0 ||
                                    BigDecimal.valueOf(bidAskBrokerDTO.askPrice1()).divide(BigDecimal.valueOf(bidAskBrokerDTO.lastClose()), 4, RoundingMode.HALF_EVEN).compareTo(stopLoss) <= 0) {
                                List<OrderDTO> orders = orderService.sell(positions.stream().filter(position -> position.getSecurityCode().equals(bidAskBrokerDTO.securityCode())).toList());
                                if (!orders.isEmpty()) {
                                    log.warn("BidAsk hit: {}", bidAskBrokerDTO);
                                    orderService.addOrder(orders.get(0), portfolio.getName(), LocalDateTime.now(), LocalDateTime.now().plusMinutes(1L));
                                }
                            }
                        }
                );
            }

        });
    }

    @Scheduled(fixedDelay = 6000L)
    public void updateMainOrder() {
        portfolioService.listPortfolioDTO().forEach(portfolioDTO -> {
            List<Order> orders = orderService.listOrders(portfolioDTO.name());
            Portfolio portfolio = portfolioService.getPortfolio(portfolioDTO.name());
            Set<String> codes = orders.stream().map(Order::getSecurityCode).collect(Collectors.toSet());
            codes.forEach(code -> {
                PositionIntegrateDTO position = orderPlacementService.checkPosition(code);
                if (position != null) {
                    if (position.vol() != null) {
                        Optional<Position> existingPosition = portfolio.getPositions().stream().filter(p -> p.getSecurityCode().equals(code)).findFirst();
                        if (existingPosition.isEmpty()) {
                            Position currentPosition = new Position();
                            currentPosition.setSecurityCode(code);
                            currentPosition.setSecurityShare(Long.valueOf(position.vol()));
                            currentPosition.setCost(BigDecimal.valueOf(position.unitCost()).multiply(BigDecimal.valueOf(currentPosition.getSecurityShare())).doubleValue());
                            currentPosition.setMarketValue(position.marketValue());
                            portfolioService.updatePosition(currentPosition);
                            portfolio.getPositions().add(currentPosition);
                        } else {
                            Position currentPosition = existingPosition.get();
                            currentPosition.setSecurityShare(Long.valueOf(position.vol()));
                            currentPosition.setCost(BigDecimal.valueOf(position.unitCost()).multiply(BigDecimal.valueOf(currentPosition.getSecurityShare())).doubleValue());
                            currentPosition.setMarketValue(position.marketValue());
                            portfolioService.updatePosition(currentPosition);
                        }
                    } else {
                        // Remove positions
                        Optional<Position> existingPosition = portfolio.getPositions().stream().filter(p -> p.getSecurityCode().equals(code)).findFirst();
                        if (existingPosition.isPresent()) {
                            portfolio.setPositions(portfolio.getPositions().stream().filter(p -> !p.getSecurityCode().equals(code)).toList());
                            portfolioService.updatePortfolio(portfolio);
                            portfolioService.deletePosition(existingPosition.get());
                        }
                    }
                }
            });
            orders.forEach(order -> {
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

            double todayTradeTotal = orderPlacementService.listTodayTrades().stream().mapToDouble(TradeDTO::amount).sum();
            Dynamics dynamics = portfolioService.getDynamics(portfolio);
            dynamics.setCash(BigDecimal.valueOf(dynamics.getLastDayCash()).subtract(BigDecimal.valueOf(todayTradeTotal)).doubleValue());
            dynamics.setSecurityMarketValue(portfolio.getPositions().stream().mapToDouble(Position::getMarketValue).sum());
            dynamics.setTotalMarketValue(BigDecimal.valueOf(dynamics.getCash()).add(BigDecimal.valueOf(dynamics.getSecurityMarketValue())).doubleValue());
            portfolioService.updateDynamics(dynamics);
//            portfolioService.appendPositions(portfolioDTO, positions);
        });
    }

    private boolean isBetween(LocalDateTime startTime, LocalDateTime endTime) {
        LocalDateTime now = LocalDateTime.now();
        return !now.isBefore(startTime) && !now.isAfter(endTime);
    }

    private boolean isTradeTime() {
        LocalDateTime now = LocalDateTime.now();
        return !now.toLocalTime().isBefore(LocalTime.of(9, 30, 0)) && !now.toLocalTime().isAfter(LocalTime.of(14, 50, 0))
                && !now.toLocalDate().getDayOfWeek().equals(DayOfWeek.SATURDAY) && !now.toLocalDate().getDayOfWeek().equals(DayOfWeek.SUNDAY);
    }

}
