package com.portfolio.manager.task;

import com.portfolio.manager.constant.Constant;
import com.portfolio.manager.domain.*;
import com.portfolio.manager.domain.strategy_specific.PositionBookForCrown;
import com.portfolio.manager.dto.BidAskBrokerDTO;
import com.portfolio.manager.dto.OrderDTO;
import com.portfolio.manager.integration.MarketDataClient;
import com.portfolio.manager.repository.PositionBookForCrownRepo;
import com.portfolio.manager.service.OrderService;
import com.portfolio.manager.service.PortfolioService;
import com.portfolio.manager.util.Util;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
    PortfolioService portfolioService;

    @Resource
    MarketDataClient marketDataClient;

    @Resource
    PositionBookForCrownRepo positionBookForCrownRepo;

    @Scheduled(fixedDelay = 1000L)
    public void placeOrder() {
        portfolioService.listPortfolioDTO().forEach(portfolioDTO -> {
            //Order execution
            List<Order> orders = orderService.listPendingOrders(portfolioDTO.name());
            Set<String> securityCodes = new HashSet<>();

            orders.forEach(order -> {
                List<SubOrder> subOrders = order.getSubOrders().stream().filter(subOrder -> this.isBetween(subOrder.getStartTime(), subOrder.getEndTime()) && subOrder.getRemainingShare() > 0).toList();
                if (!subOrders.isEmpty()) {
                    securityCodes.addAll(subOrders.stream().map(SubOrder::getSecurityCode).collect(Collectors.toSet()));
                }
            });

            if (!securityCodes.isEmpty()) {
                Map<String, BidAskBrokerDTO> bidAsks = marketDataClient.getBidAsk(securityCodes.stream().toList()).stream().collect(Collectors.toMap(BidAskBrokerDTO::securityCode, Function.identity()));
                log.info("bid ask: {}", bidAsks);
                orders.stream().parallel().forEach(order -> {
                    List<SubOrder> subOrders = order.getSubOrders().stream().filter(subOrder -> this.isBetween(subOrder.getStartTime(), subOrder.getEndTime()) && subOrder.getRemainingShare() > 0).toList();
                    if (!subOrders.isEmpty()) {
                        subOrders.stream().parallel().forEach(subOrder -> {
                            if (order.getBuyOrSell().equals(Direction.买入)) {
                                if (bidAsks.get(order.getSecurityCode()).askVol1() >= subOrder.getRemainingShare()) {
                                    orderService.execute(subOrder, order.getId(), bidAsks.get(order.getSecurityCode()).askPrice1(), subOrder.getRemainingShare().intValue());
                                } else {
                                    orderService.execute(subOrder, order.getId(), bidAsks.get(order.getSecurityCode()).askPrice1(), bidAsks.get(order.getSecurityCode()).askVol1());
                                }
                            } else if (order.getBuyOrSell().equals(Direction.卖出)) {
                                if (bidAsks.get(order.getSecurityCode()).bidVol1() >= subOrder.getRemainingShare()) {
                                    orderService.execute(subOrder, order.getId(), bidAsks.get(order.getSecurityCode()).bidPrice1(), subOrder.getRemainingShare().intValue());
                                } else {
                                    orderService.execute(subOrder, order.getId(), bidAsks.get(order.getSecurityCode()).bidPrice1(), bidAsks.get(order.getSecurityCode()).bidVol1());
                                }
                            }
                        });
                    }
                });
            }
            Portfolio portfolio = portfolioService.getPortfolio(portfolioDTO.name());
            // Update position
            portfolioService.syncUpPositionsAndDynamicsWithBroker(portfolio);
            // Update order
            orderService.updateOrders(portfolio);
        });
    }

    @Scheduled(cron = "0 50 14 ? * MON-FRI")
    public void buyBackForCrown() {
        final double buyBackDiscount = 0.5d;
        final double buyBackHalfBar = 0.2d;
        portfolioService.listPortfolio().stream().filter(Portfolio::getTakeProfitStopLoss
        ).toList().forEach(portfolio -> {
            Map<String, Position> position = portfolioService.listPosition(portfolio.getName()).stream().collect(Collectors.toMap(Position::getSecurityCode, Function.identity()));
            positionBookForCrownRepo.findByPortfolioName(portfolio.getName()).stream().filter(PositionBookForCrown::getBuyBack).parallel().forEach(positionBookForCrown -> {
                var currentPosition = position.get(positionBookForCrown.getSecurityCode());
                if (currentPosition == null ||
                        BigDecimal.valueOf(currentPosition.getSecurityShare()).divide(BigDecimal.valueOf(positionBookForCrown.getSecurityShare()), 2, RoundingMode.HALF_UP).compareTo(BigDecimal.valueOf(buyBackHalfBar)) < 0) {
                    positionBookForCrown.setSecurityShare(Util.calVolume(positionBookForCrown.getSecurityShare(), buyBackDiscount, Constant.CONVERTIBLE_BOND_MULTIPLE));
                    log.warn("Buy back hit: {}", positionBookForCrown);
                    if (currentPosition != null) {
                        positionBookForCrown.setSecurityShare(positionBookForCrown.getSecurityShare() - currentPosition.getSecurityShare());
                    }
                    OrderDTO orderDTO = new OrderDTO(Direction.买入, positionBookForCrown.getSecurityShare(), positionBookForCrown.getSecurityName(), positionBookForCrown.getSecurityCode(), 0.0d);
                    orderService.addOrder(orderDTO, portfolio, LocalDateTime.now(), LocalDateTime.now().plusMinutes(5L));
                } else if (currentPosition.getSecurityShare().compareTo(positionBookForCrown.getSecurityShare()) < 0) {
                    log.warn("Buy back hit: {}, difference: {}", positionBookForCrown, positionBookForCrown.getSecurityShare() - position.get(positionBookForCrown.getSecurityCode()).getSecurityShare());
                    OrderDTO orderDTO = new OrderDTO(Direction.买入, positionBookForCrown.getSecurityShare() - position.get(positionBookForCrown.getSecurityCode()).getSecurityShare(), positionBookForCrown.getSecurityName(), positionBookForCrown.getSecurityCode(), 0.0d);
                    orderService.addOrder(orderDTO, portfolio, LocalDateTime.now(), LocalDateTime.now().plusMinutes(5L));
                }
            });

        });
    }

    // Take profit and stop loss only
    @Scheduled(fixedDelay = 1000L)
    public void autoSell() {
        if (!isTradeTime()) {
            return;
        }
        portfolioService.listPortfolio().stream().filter(Portfolio::getTakeProfitStopLoss
        ).parallel().forEach(portfolio -> {
            List<Position> positions = portfolioService.listPosition(portfolio.getName());
            List<String> codes = positions.stream().map(Position::getSecurityCode).toList();
            if (!codes.isEmpty()) {
                marketDataClient.getBidAsk(codes).forEach(
                        bidAskBrokerDTO -> {
                            if (BigDecimal.valueOf(bidAskBrokerDTO.bidPrice1()).divide(BigDecimal.valueOf(bidAskBrokerDTO.lastClose()), 4, RoundingMode.HALF_EVEN).compareTo(Constant.CROWN_TAKE_PROFIT) >= 0 ||
                                    BigDecimal.valueOf(bidAskBrokerDTO.askPrice1()).divide(BigDecimal.valueOf(bidAskBrokerDTO.lastClose()), 4, RoundingMode.HALF_EVEN).compareTo(Constant.CROWN_STOP_LOSS) <= 0) {
                                positionBookForCrownRepo.findByPortfolioNameAndSecurityCode(portfolio.getName(), bidAskBrokerDTO.securityCode()).ifPresent(
                                        book -> {
                                            if (!book.getSellLock()) {
                                                List<OrderDTO> orders = orderService.sell(positions.stream().filter(position -> position.getSecurityCode().equals(bidAskBrokerDTO.securityCode())).toList());
                                                if (!orders.isEmpty()) {
                                                    log.warn("BidAsk hit: {}", bidAskBrokerDTO);
                                                    orderService.addOrder(orders.get(0), portfolio, LocalDateTime.now(), LocalDateTime.now().plusMinutes(1L));
                                                    book.setSellLock(true);
                                                    positionBookForCrownRepo.save(book);
                                                }
                                            }
                                        });
                            }
                        }
                );
            }

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

    public static boolean isOrderTime() {
        LocalDateTime now = LocalDateTime.now();
        return !now.toLocalTime().isBefore(LocalTime.of(9, 26, 0)) && !now.toLocalTime().isAfter(LocalTime.of(23, 50, 0));
    }

}
