package com.portfolio.manager.task;

import com.portfolio.manager.constant.Constant;
import com.portfolio.manager.domain.*;
import com.portfolio.manager.domain.strategy_specific.PositionBookForCrown;
import com.portfolio.manager.dto.BidAskBrokerDTO;
import com.portfolio.manager.dto.OrderDTO;
import com.portfolio.manager.integration.MarketDataClient;
import com.portfolio.manager.integration.OrderPlacementClient;
import com.portfolio.manager.notification.Notification;
import com.portfolio.manager.repository.*;
import com.portfolio.manager.service.OrderService;
import com.portfolio.manager.service.PortfolioService;
import com.portfolio.manager.service.PositionSnapshotService;
import com.portfolio.manager.service.sell.CrownSellStrategy;
import com.portfolio.manager.util.Util;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Slf4j
public class TradeTask {
    @Resource
    TradeRepo tradeRepo;

    @Resource
    NavRepo navRepo;

    @Resource
    SubOrderRepo subOrderRepo;

    @Resource
    OrderService orderService;

    @Resource
    PortfolioService portfolioService;

    @Resource
    MarketDataClient marketDataClient;

    @Resource
    OrderPlacementClient orderPlacementClient;

    @Resource
    PositionBookForCrownRepo positionBookForCrownRepo;

    @Resource
    PositionSnapshotService positionSnapshotService;

    @Resource
    CbStockMappingRepo cbStockMappingRepo;

    List<Nav> currentNavs;

    @Resource
    @Qualifier("WechatPublicAccount")
    Notification wechatPublicAccount;

    public static Set<String> sellLockSet = new ConcurrentSkipListSet<>();

    public static Map<String, CrownSellStrategy> cbSellStrategyMapping = new ConcurrentHashMap<>();

    @Scheduled(fixedDelay = 1000L)
    public void placeOrder() {
        portfolioService.listPortfolioDTO().forEach(portfolioDTO -> {
            //Order execution
            List<Order> orders = orderService.listPendingOrders(portfolioDTO.name());
            Set<String> securityCodes = orders.stream().flatMap(order -> order.getSubOrders().stream().filter(subOrder -> this.isBetween(subOrder.getStartTime(), subOrder.getEndTime()) && subOrder.getRemainingShare() > 0)
                    .map(SubOrder::getSecurityCode)).collect(Collectors.toSet());

            if (!securityCodes.isEmpty()) {
                Map<String, BidAskBrokerDTO> bidAsks = marketDataClient.getBidAsk(securityCodes.stream().toList()).stream().collect(Collectors.toMap(BidAskBrokerDTO::securityCode, Function.identity()));
                log.info("bid ask: {}", bidAsks);
                orders.stream().parallel().forEach(order -> {
                    var subOrders = order.getSubOrders().stream().filter(subOrder -> this.isBetween(subOrder.getStartTime(), subOrder.getEndTime()) && subOrder.getRemainingShare() > 0).toList();
                    if (!subOrders.isEmpty()) {
                        subOrders.stream().parallel().forEach(subOrder -> {
                            if (order.getBuyOrSell().equals(Direction.买入)) {
                                positionBookForCrownRepo.findByPortfolioNameAndSecurityCode(portfolioDTO.name(), order.getSecurityCode()).ifPresent(positionBook -> {
                                    if (!positionBook.getBuyLock()) {
                                        orderService.execute(subOrder, order.getId(), bidAsks.get(order.getSecurityCode()).askPrice1(),
                                                Math.min(subOrder.getRemainingShare().intValue(), bidAsks.get(order.getSecurityCode()).askVol1()));
                                    } else {
                                        log.info("Buy lock");
                                    }
                                });
                            } else if (order.getBuyOrSell().equals(Direction.卖出)) {
                                orderService.execute(subOrder, order.getId(), bidAsks.get(order.getSecurityCode()).bidPrice1(), Math.min(subOrder.getRemainingShare().intValue(), bidAsks.get(order.getSecurityCode()).bidVol1()));
                            }
                        });
                    }
                });
            }
            var cancelableOrders = orderPlacementClient.queryCancelableOrders();
            cancelableOrders.forEach(cancelableOrder -> {
                Optional<Trade> trade = tradeRepo.findByClientOrderIdOrderByCreateTimeDesc(cancelableOrder.orderId()).stream().findFirst();
                trade.ifPresent(item -> {
                    if (item.getCreateTime().plusSeconds(30L).isBefore(LocalDateTime.now())) {
                        boolean result = orderPlacementClient.cancelOrder(cancelableOrder.orderId());
                        if (result) {
                            Optional<SubOrder> subOrder = subOrderRepo.findById(item.getSubOrderId());
                            subOrder.ifPresent(subOrderItem -> {
                                subOrderItem.setRemainingShare(Long.valueOf(cancelableOrder.cancelableVolume()));
                                subOrderItem.setEndTime(LocalDateTime.now().plusMinutes(1L));
                                subOrderRepo.save(subOrderItem);
                            });
                            item.setVolume(item.getVolume() - cancelableOrder.cancelableVolume());
                            tradeRepo.save(item);
                        }
                    }
                });
            });
            Portfolio portfolio = portfolioService.getPortfolio(portfolioDTO.name());
            // Update position
            portfolioService.syncUpPositionsAndDynamicsWithBroker(portfolio);
            // Update order
            orderService.updateOrders(portfolio);
        });

        currentNavs = portfolioService.listNavs();
    }

    @Scheduled(cron = "0 50 14 ? * MON-FRI")
    public void buyBackForCrown() {
        final double BUY_BACK_DISCOUNT = 0.5d;
        final double BUY_BACK_HALF_BAR = 0.2d;
        portfolioService.listPortfolio().stream().filter(Portfolio::getTakeProfitStopLoss
        ).toList().forEach(portfolio -> {
            // Unlock all buy orders
            List<PositionBookForCrown> positionBookForCrownList = positionBookForCrownRepo.findByPortfolioName(portfolio.getName());
            positionBookForCrownList.forEach(positionBookForCrown -> positionBookForCrown.setBuyLock(false));
            positionBookForCrownRepo.saveAll(positionBookForCrownList);

            List<Position> positions = portfolioService.listPosition(portfolio.getName());
            if (positions.isEmpty()) {
                positionSnapshotService.get().forEach(positionSnapshot -> {
                    OrderDTO orderDTO = new OrderDTO(Direction.买入, positionSnapshot.getSecurityShare(), "", positionSnapshot.getSecurityCode(), 0.0d);
                    orderService.addOrder(orderDTO, portfolio, LocalDateTime.now(), LocalDateTime.now().plusMinutes(6L));
                    wechatPublicAccount.send("Portfolio Buy back", positionSnapshot.getSecurityCode());
                });
                return;
            }

            Map<String, Position> positionMap = positions.stream().collect(Collectors.toMap(Position::getSecurityCode, Function.identity()));
            positionBookForCrownRepo.findByPortfolioName(portfolio.getName()).stream().filter(PositionBookForCrown::getBuyBack).parallel().forEach(positionBookForCrown -> {
                var currentPosition = positionMap.get(positionBookForCrown.getSecurityCode());
                if (currentPosition == null ||
                        BigDecimal.valueOf(currentPosition.getSecurityShare()).divide(BigDecimal.valueOf(positionBookForCrown.getSecurityShare()), 2, RoundingMode.HALF_UP).compareTo(BigDecimal.valueOf(BUY_BACK_HALF_BAR)) < 0) {
                    positionBookForCrown.setSecurityShare(Util.calVolume(positionBookForCrown.getSecurityShare(), BUY_BACK_DISCOUNT, Constant.CONVERTIBLE_BOND_MULTIPLE));
                    if (currentPosition != null) {
                        positionBookForCrown.setSecurityShare(positionBookForCrown.getSecurityShare() - currentPosition.getSecurityShare());
                    }
                    OrderDTO orderDTO = new OrderDTO(Direction.买入, positionBookForCrown.getSecurityShare(), positionBookForCrown.getSecurityName(), positionBookForCrown.getSecurityCode(), 0.0d);
                    orderService.addOrder(orderDTO, portfolio, LocalDateTime.now(), LocalDateTime.now().plusMinutes(6L));
                    wechatPublicAccount.send("First half buy back hit", positionBookForCrown.getSecurityName());
                } else if (currentPosition.getSecurityShare().compareTo(positionBookForCrown.getSecurityShare()) < 0) {
                    log.warn("Buy back hit: {}, difference: {}", positionBookForCrown, positionBookForCrown.getSecurityShare() - positionMap.get(positionBookForCrown.getSecurityCode()).getSecurityShare());
                    OrderDTO orderDTO = new OrderDTO(Direction.买入, positionBookForCrown.getSecurityShare() - positionMap.get(positionBookForCrown.getSecurityCode()).getSecurityShare(), positionBookForCrown.getSecurityName(), positionBookForCrown.getSecurityCode(), 0.0d);
                    orderService.addOrder(orderDTO, portfolio, LocalDateTime.now(), LocalDateTime.now().plusMinutes(6L));
                    wechatPublicAccount.send("Second half buy back hit", positionBookForCrown.getSecurityName());
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
//                            if (BigDecimal.valueOf(bidAskBrokerDTO.bidPrice1()).divide(BigDecimal.valueOf(bidAskBrokerDTO.lastClose()), 4, RoundingMode.HALF_EVEN).compareTo(Constant.CROWN_TAKE_PROFIT) >= 0 ||
//                                    BigDecimal.valueOf(bidAskBrokerDTO.high()).divide(BigDecimal.valueOf(bidAskBrokerDTO.lastClose()), 4, RoundingMode.HALF_EVEN).compareTo(Constant.CROWN_TAKE_PROFIT) >= 0 ||
//                                    BigDecimal.valueOf(bidAskBrokerDTO.askPrice1()).divide(BigDecimal.valueOf(bidAskBrokerDTO.lastClose()), 4, RoundingMode.HALF_EVEN).compareTo(TradeTask.getStopLossBar()) <= 0) {
                            if (this.isSellable(bidAskBrokerDTO)) {
                                positionBookForCrownRepo.findByPortfolioNameAndSecurityCode(portfolio.getName(), bidAskBrokerDTO.securityCode()).ifPresentOrElse(
                                        book -> {
                                            if (!book.getSellLock()) {
                                                List<OrderDTO> orders = orderService.sell(positions.stream().filter(position -> position.getSecurityCode().equals(bidAskBrokerDTO.securityCode())).toList());
                                                orders.forEach(order -> {
                                                    log.warn("BidAsk hit: {}", bidAskBrokerDTO);
                                                    orderService.addOrder(order, portfolio, LocalDateTime.now(), LocalDateTime.now().plusMinutes(1L));
                                                    book.setSellLock(true);
                                                    book.setBuyLock(true);
                                                    positionBookForCrownRepo.save(book);
                                                    wechatPublicAccount.send("Stop hit", bidAskBrokerDTO.toString());
                                                });
                                            }
                                        }, () -> {
                                            List<Position> selectedPositions = positions.stream().filter(position -> position.getSecurityCode().equals(bidAskBrokerDTO.securityCode())).toList();
                                            this.handleStopLoss(selectedPositions, portfolio, "Stop hit");
                                        });
                            }
                        }
                );
                // 2nd tier stop loss check
                if (currentNavs != null) {
                    var currentNav = currentNavs.stream().filter(nav -> nav.getPortfolioName().equals(portfolio.getName())).findFirst();
                    currentNav.ifPresent(nav -> navRepo.findFirstByPortfolioNameOrderByCreateTimeDesc(portfolio.getName()).ifPresent(
                            lastNav -> {
                                if (nav.getNav().divide(lastNav.getNav(), 4, RoundingMode.HALF_UP).compareTo(Constant.CROWN_WHOLE_PORTFOLIO_STOP_LOSS_EXCEPTION) > 0 && nav.getNav().divide(lastNav.getNav(), 4, RoundingMode.HALF_UP).compareTo(Constant.CROWN_WHOLE_PORTFOLIO_STOP_LOSS) <= 0) {
                                    log.info("The whole portfolio is reaching stop loss line");
                                    this.handleStopLoss(positions, portfolio, "The whole portfolio is reaching stop loss line");
                                }
                            }
                    ));
                }
            }
        });
    }

    private boolean isBetween(LocalDateTime startTime, LocalDateTime endTime) {
        LocalDateTime now = LocalDateTime.now();
        return !now.isBefore(startTime) && !now.isAfter(endTime);
    }

    private boolean isTradeTime() {
        LocalDateTime now = LocalDateTime.now();
        return !now.toLocalTime().isBefore(LocalTime.of(9, 30, 30)) && !now.toLocalTime().isAfter(LocalTime.of(14, 50, 0))
                && !now.toLocalDate().getDayOfWeek().equals(DayOfWeek.SATURDAY) && !now.toLocalDate().getDayOfWeek().equals(DayOfWeek.SUNDAY);
//        return !now.toLocalTime().isBefore(LocalTime.of(9, 30, 30)) && !now.toLocalTime().isAfter(LocalTime.of(14, 50, 0));
    }

    public static boolean isOrderTime() {
        LocalTime now = LocalDateTime.now().toLocalTime();
        return !now.isBefore(LocalTime.of(9, 26, 0)) && !now.isAfter(LocalTime.of(23, 50, 0));
    }

    public static BigDecimal getStopLossBar() {
        LocalTime now = LocalDateTime.now().toLocalTime();
        if (!now.isBefore(LocalTime.of(9, 30, 30)) && !now.isAfter(LocalTime.of(10, 30, 30))) {
            return new BigDecimal("0.97");
        }
        return Constant.CROWN_STOP_LOSS;
    }

    public boolean isSellable(BidAskBrokerDTO bidAskBrokerDTO) {
        log.info("price: {}", bidAskBrokerDTO);
        if (cbSellStrategyMapping.containsKey(bidAskBrokerDTO.securityCode())) {
            var strategy = cbSellStrategyMapping.get(bidAskBrokerDTO.securityCode());
            strategy.updateState(bidAskBrokerDTO);
        } else {
            CrownSellStrategy strategy = new CrownSellStrategy(marketDataClient, cbStockMappingRepo);
            strategy.updateState(bidAskBrokerDTO);
            cbSellStrategyMapping.put(bidAskBrokerDTO.securityCode(), strategy);
        }
        log.info("strategy map: {}", cbSellStrategyMapping.get(bidAskBrokerDTO.securityCode()));
        return cbSellStrategyMapping.get(bidAskBrokerDTO.securityCode()).isSellable();
    }

    private void handleStopLoss(List<Position> selectedPositions, Portfolio portfolio, String notificationTitle) {
        List<OrderDTO> orders = orderService.sell(selectedPositions);
        orders.forEach(order -> {
            if (!sellLockSet.contains(order.securityCode())) {
                orderService.addOrder(order, portfolio, LocalDateTime.now(), LocalDateTime.now().plusMinutes(1L));
                wechatPublicAccount.send(notificationTitle, order.toString());
                sellLockSet.add(order.securityCode());
                positionBookForCrownRepo.findByPortfolioNameAndSecurityCode(portfolio.getName(), order.securityCode()).ifPresent(book -> {
                    book.setSellLock(true);
                    book.setBuyLock(true);
                    positionBookForCrownRepo.save(book);
                });
            }
        });
    }

}
