package com.portfolio.manager.task;

import com.portfolio.manager.constant.Constant;
import com.portfolio.manager.domain.*;
import com.portfolio.manager.domain.strategy_specific.PositionBookForCrown;
import com.portfolio.manager.dto.BidAskBrokerDTO;
import com.portfolio.manager.dto.OrderDTO;
import com.portfolio.manager.integration.MarketDataService;
import com.portfolio.manager.repository.PositionBookForCrownRepo;
import com.portfolio.manager.service.AlgoService;
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
import java.time.LocalDate;
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
    PortfolioService portfolioService;

    @Resource
    AlgoService algoService;

    @Resource
    MarketDataService marketDataService;

    @Resource
    PositionBookForCrownRepo positionBookForCrownRepo;

    @Scheduled(fixedDelay = 1000L)
    public void placeOrder() {
        LocalDate today = LocalDate.now();
        portfolioService.listPortfolioDTO().forEach(portfolioDTO -> {
            //Order execution
//            List<Order> orders = orderService.listOrders(portfolioDTO.name());
            List<Order> orders = orderService.listPendingOrders(portfolioDTO.name());
            Set<String> securityCodes = new HashSet<>();

            Set<String> securityCodesOfOrders = new HashSet<>();
            orders.forEach(order -> {
                List<SubOrder> subOrders = order.getSubOrders().stream().filter(subOrder -> this.isBetween(subOrder.getStartTime(), subOrder.getEndTime()) && subOrder.getRemainingShare() > 0).toList();
                if (!subOrders.isEmpty()) {
                    securityCodes.addAll(subOrders.stream().map(SubOrder::getSecurityCode).collect(Collectors.toSet()));
                }
                if (order.getUpdateTime().toLocalDate().isEqual(today)) {
                    securityCodesOfOrders.add(order.getSecurityCode());
                }
            });

            if (!securityCodes.isEmpty()) {
                Map<String, BidAskBrokerDTO> bidAsks = marketDataService.getBidAsk(securityCodes.stream().toList()).stream().collect(Collectors.toMap(BidAskBrokerDTO::securityCode, Function.identity()));
                orders.stream().parallel().forEach(order -> {
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
            Portfolio portfolio = portfolioService.getPortfolio(portfolioDTO.name());
            // Update position
            portfolioService.syncUpPositions(portfolio, securityCodesOfOrders);
            // Update order
            orderService.updateOrders(portfolio);
        });
    }

    @Scheduled(cron = "0 50 14 ? * MON-FRI")
    public void buyBackForCrown() {
        double buyBackDiscount = 0.5d;
        portfolioService.listPortfolio().stream().filter(Portfolio::getTakeProfitStopLoss
        ).toList().forEach(portfolio -> {
            Map<String, Position> position = portfolioService.listPosition(portfolio.getName()).stream().collect(Collectors.toMap(Position::getSecurityCode, Function.identity()));
            positionBookForCrownRepo.findByPortfolioName(portfolio.getName()).stream().parallel().forEach(positionBookForCrown -> {
                if (positionBookForCrown.getBuyBack()) {
                    if (position.get(positionBookForCrown.getSecurityCode()) == null) {
                        positionBookForCrown.setSecurityShare(Util.calVolume(positionBookForCrown.getSecurityShare(), buyBackDiscount, Constant.CONVERTIBLE_BOND_MULTIPLE));
                        log.warn("Buy back hit: {}", positionBookForCrown);
                        OrderDTO orderDTO = new OrderDTO(Direction.买入, positionBookForCrown.getSecurityShare(), positionBookForCrown.getSecurityName(), positionBookForCrown.getSecurityCode(), 0.0d);
                        orderService.addOrder(orderDTO, portfolio.getName(), LocalDateTime.now(), LocalDateTime.now().plusMinutes(5L));
                    } else if (position.get(positionBookForCrown.getSecurityCode()).getSecurityShare().compareTo(positionBookForCrown.getSecurityShare()) < 0) {
                        log.warn("Buy back hit: {}, difference: {}", positionBookForCrown, positionBookForCrown.getSecurityShare() - position.get(positionBookForCrown.getSecurityCode()).getSecurityShare());
                        OrderDTO orderDTO = new OrderDTO(Direction.买入, positionBookForCrown.getSecurityShare() - position.get(positionBookForCrown.getSecurityCode()).getSecurityShare(), positionBookForCrown.getSecurityName(), positionBookForCrown.getSecurityCode(), 0.0d);
                        orderService.addOrder(orderDTO, portfolio.getName(), LocalDateTime.now(), LocalDateTime.now().plusMinutes(5L));
                    }
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
                marketDataService.getBidAsk(codes).forEach(
                        bidAskBrokerDTO -> {
                            if (BigDecimal.valueOf(bidAskBrokerDTO.bidPrice1()).divide(BigDecimal.valueOf(bidAskBrokerDTO.lastClose()), 4, RoundingMode.HALF_EVEN).compareTo(Constant.CROWN_TAKE_PROFIT) >= 0 ||
                                    BigDecimal.valueOf(bidAskBrokerDTO.askPrice1()).divide(BigDecimal.valueOf(bidAskBrokerDTO.lastClose()), 4, RoundingMode.HALF_EVEN).compareTo(Constant.CROWN_STOP_LOSS) <= 0) {
//                                Optional<PositionBookForCrown> book = positionBookForCrownRepo.findByPortfolioNameAndSecurityCode(portfolio.getName(), bidAskBrokerDTO.securityCode());
//                                if (book.isPresent() && !book.get().getSellLock()) {
//                                    List<OrderDTO> orders = orderService.sell(positions.stream().filter(position -> position.getSecurityCode().equals(bidAskBrokerDTO.securityCode())).toList());
//                                    if (!orders.isEmpty()) {
//                                        log.warn("BidAsk hit: {}", bidAskBrokerDTO);
//                                        orderService.addOrder(orders.get(0), portfolio.getName(), LocalDateTime.now(), LocalDateTime.now().plusMinutes(1L));
//                                        PositionBookForCrown positionBook = book.get();
//                                        positionBook.setSellLock(true);
//                                        positionBookForCrownRepo.save(positionBook);
//                                    }
//                                }

                                positionBookForCrownRepo.findByPortfolioNameAndSecurityCode(portfolio.getName(), bidAskBrokerDTO.securityCode()).ifPresent(
                                        book -> {
                                            if (!book.getSellLock()) {
                                                List<OrderDTO> orders = orderService.sell(positions.stream().filter(position -> position.getSecurityCode().equals(bidAskBrokerDTO.securityCode())).toList());
                                                if (!orders.isEmpty()) {
                                                    log.warn("BidAsk hit: {}", bidAskBrokerDTO);
                                                    orderService.addOrder(orders.get(0), portfolio.getName(), LocalDateTime.now(), LocalDateTime.now().plusMinutes(1L));
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
