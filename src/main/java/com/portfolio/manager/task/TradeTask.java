package com.portfolio.manager.task;

import com.portfolio.manager.constant.Constant;
import com.portfolio.manager.data.PositionData;
import com.portfolio.manager.domain.*;
import com.portfolio.manager.domain.strategy_specific.PositionBookForCrown;
import com.portfolio.manager.dto.integration.BidAskBrokerDTO;
import com.portfolio.manager.dto.ui.OrderDTO;
import com.portfolio.manager.factory.OrderExecutionFactory;
import com.portfolio.manager.integration.MarketDataClient;
import com.portfolio.manager.notification.Notification;
import com.portfolio.manager.repository.CbStockMappingRepo;
import com.portfolio.manager.repository.NavRepo;
import com.portfolio.manager.repository.PositionBookForCrownRepo;
import com.portfolio.manager.service.OrderService;
import com.portfolio.manager.service.PortfolioService;
import com.portfolio.manager.service.PositionSnapshotService;
import com.portfolio.manager.service.new_sell.State;
import com.portfolio.manager.service.sell.CrownSellStrategy;
import com.portfolio.manager.service.sell.VWAP;
import com.portfolio.manager.service.tracking.SecurityToTrack;
import com.portfolio.manager.service.tracking.TrackingService;
import com.portfolio.manager.util.Util;
import jakarta.annotation.PostConstruct;
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
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Slf4j
public class TradeTask {
    @Resource
    NavRepo navRepo;

    @Resource
    OrderService orderService;

    @Resource
    PortfolioService portfolioService;

    @Resource
    MarketDataClient marketDataClient;

    @Resource
    @Qualifier("NormalOrderExecutionFactory")
    OrderExecutionFactory orderExecutionFactory;

    @Resource
    PositionBookForCrownRepo positionBookForCrownRepo;

    @Resource
    PositionSnapshotService positionSnapshotService;

    @Resource
    PositionData positionData;

    @Resource
    CbStockMappingRepo cbStockMappingRepo;

    List<Nav> currentNavs;

    @Resource
    TrackingService trackingService;

    @Resource
    @Qualifier("WechatPublicAccount")
    Notification wechatPublicAccount;

    @Resource
    VWAP vwap;

    public static Set<String> sellLockSet = new ConcurrentSkipListSet<>();

    public static Map<String, SecurityToTrack> securityToTrackMap = new ConcurrentHashMap<>();

    public static Map<String, Queue<State>> securityStatesTrackingQueue = new ConcurrentHashMap<>();

    public static Map<String, CrownSellStrategy> cbSellStrategyMapping = new ConcurrentHashMap<>();

//    更新持仓数据等
    @PostConstruct
    @Scheduled(fixedDelay = 1000L)
    public void queryMarket() {
        positionData.update();
    }

    @Scheduled(fixedDelay = 1000L)
    public void placeOrder() {
        portfolioService.listPortfolioDTO().forEach(portfolioDTO -> {
            orderExecutionFactory.executeOrders(portfolioDTO.name());
//            //Order execution
//            List<Order> orders = orderService.listPendingOrders(portfolioDTO.name());
//            Set<String> securityCodes = orders.stream().flatMap(order -> order.getSubOrders().stream().filter(subOrder -> this.isBetween(subOrder.getStartTime(), subOrder.getEndTime()) && subOrder.getRemainingShare() > 0)
//                    .map(SubOrder::getSecurityCode)).collect(Collectors.toSet());
//
//            if (!securityCodes.isEmpty()) {
//                Map<String, BidAskBrokerDTO> bidAsks = marketDataClient.getBidAsk(securityCodes.stream().toList()).stream().collect(Collectors.toMap(BidAskBrokerDTO::securityCode, Function.identity()));
//                log.info("bid ask: {}", bidAsks);
//                orders.stream().parallel().forEach(order -> {
//                    var subOrders = order.getSubOrders().stream().filter(subOrder -> this.isBetween(subOrder.getStartTime(), subOrder.getEndTime()) && subOrder.getRemainingShare() > 0).toList();
//                    if (!subOrders.isEmpty()) {
//                        subOrders.stream().parallel().forEach(subOrder -> {
//                            if (order.getBuyOrSell().equals(Direction.买入)) {
////                                positionBookForCrownRepo.findByPortfolioNameAndSecurityCode(portfolioDTO.name(), order.getSecurityCode()).ifPresent(positionBook -> {
////                                    if (!positionBook.getBuyLock()) {
//                                orderService.execute(subOrder, order.getId(), bidAsks.get(order.getSecurityCode()).askPrice1(),
//                                        Math.min(subOrder.getRemainingShare().intValue(), bidAsks.get(order.getSecurityCode()).askVol1()));
////                                    } else {
////                                        log.info("Buy lock");
////                                    }
////                                });
//                            } else if (order.getBuyOrSell().equals(Direction.卖出)) {
//                                orderService.execute(subOrder, order.getId(), bidAsks.get(order.getSecurityCode()).bidPrice1(), Math.min(subOrder.getRemainingShare().intValue(), bidAsks.get(order.getSecurityCode()).bidVol1()));
//                            }
//                        });
//                    }
//                });
//            }
//            var cancelableOrders = orderPlacementClient.queryCancelableOrders();
//            cancelableOrders.forEach(cancelableOrder -> {
//                Optional<Trade> trade = tradeRepo.findByClientOrderIdOrderByCreateTimeDesc(cancelableOrder.orderId()).stream().findFirst();
//                trade.ifPresent(item -> {
//                    if (item.getCreateTime().plusSeconds(30L).isBefore(LocalDateTime.now())) {
//                        boolean result = orderPlacementClient.cancelOrder(cancelableOrder.orderId());
//                        if (result) {
//                            Optional<SubOrder> subOrder = subOrderRepo.findById(item.getSubOrderId());
//                            subOrder.ifPresent(subOrderItem -> {
//                                subOrderItem.setRemainingShare(Long.valueOf(cancelableOrder.cancelableVolume()));
//                                subOrderItem.setEndTime(LocalDateTime.now().plusMinutes(1L));
//                                subOrderRepo.save(subOrderItem);
//                            });
//                            item.setVolume(item.getVolume() - cancelableOrder.cancelableVolume());
//                            tradeRepo.save(item);
//                        }
//                    }
//                });
//            });
            Portfolio portfolio = portfolioService.getPortfolio(portfolioDTO.name());
            // Update position
            portfolioService.syncUpPositionsAndDynamicsWithBroker(portfolio);
            // Update order
            orderService.updateOrders(portfolio);
        });

        currentNavs = portfolioService.listNavs();
    }

    //    @Scheduled(cron = "0 50 14 ? * MON-FRI")
    public void buyBackForCrown() {
        final double BUY_BACK_DISCOUNT = 0.5d;
        final double BUY_BACK_HALF_BAR = 0.2d;
        portfolioService.listPortfolio().stream().filter(Portfolio::getTakeProfitStopLoss
        ).toList().forEach(portfolio -> {
            // Unlock all buy orders
//            List<PositionBookForCrown> positionBookForCrownList = positionBookForCrownRepo.findByPortfolioName(portfolio.getName());
//            positionBookForCrownList.forEach(positionBookForCrown -> positionBookForCrown.setBuyLock(false));
//            positionBookForCrownRepo.saveAll(positionBookForCrownList);

            List<Position> positions = portfolioService.listPosition(portfolio.getName());
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
//    @Scheduled(fixedDelay = 1000L)
    public void autoSell() {
        if (isNotTradeTime()) {
            return;
        }
        portfolioService.listPortfolio().stream().filter(Portfolio::getTakeProfitStopLoss
        ).parallel().forEach(portfolio -> {
            List<Position> positions = portfolioService.listPosition(portfolio.getName());
            if (positions.isEmpty()) {
                return;
            }
            List<String> codes = positions.stream().map(Position::getSecurityCode).toList();
            marketDataClient.getBidAsk(codes).forEach(
                    bidAskBrokerDTO -> {
                        if (this.isSellable(bidAskBrokerDTO)) {
                            positionBookForCrownRepo.findByPortfolioNameAndSecurityCode(portfolio.getName(), bidAskBrokerDTO.securityCode()).ifPresentOrElse(
                                    book -> {
                                        if (!book.getSellLock()) {
                                            Optional<PositionSnapshot> posSnapShot = positionSnapshotService.get().stream().filter(position -> position.getSecurityCode().equals(bidAskBrokerDTO.securityCode())).findFirst();
                                            Optional<Position> pos = positions.stream().filter(position -> position.getSecurityCode().equals(bidAskBrokerDTO.securityCode())).findFirst();
                                            if (posSnapShot.isPresent() && pos.isPresent()) {
                                                log.info("======new stop loss======");
                                                this.handleStopLossMultiTier(pos.get(), posSnapShot.get(), portfolio, bidAskBrokerDTO, "Stop hit");
                                            } else {
                                                List<OrderDTO> orders = orderService.sell(positions.stream().filter(position -> position.getSecurityCode().equals(bidAskBrokerDTO.securityCode())).toList());
                                                orders.forEach(order -> {
                                                    log.warn("BidAsk hit: {}", bidAskBrokerDTO);
                                                    orderService.addOrder(order, portfolio, LocalDateTime.now(), LocalDateTime.now().plusMinutes(1L));
                                                    book.setSellLock(true);
//                                                    book.setBuyLock(true);
                                                    positionBookForCrownRepo.save(book);
                                                    wechatPublicAccount.send("Stop hit", bidAskBrokerDTO.toString());
                                                });
                                            }
                                        }
                                    }, () -> {
                                        List<Position> selectedPositions = positions.stream().filter(position -> position.getSecurityCode().equals(bidAskBrokerDTO.securityCode())).toList();
                                        this.handleStopLoss(selectedPositions, portfolio, "Stop hit");
                                    });
                        }
                        if (this.isSlump(bidAskBrokerDTO)) {
                            log.info("slump hit");
                        }
                    }
            );
            // 2nd tier stop loss check
            if (currentNavs != null) {
                var currentNav = currentNavs.stream().filter(nav -> nav.getPortfolioName().equals(portfolio.getName())).findFirst();
                currentNav.ifPresent(nav -> navRepo.findFirstByPortfolioNameOrderByCreateTimeDesc(portfolio.getName()).ifPresent(
                        lastNav -> {
                            if (nav.getNav().divide(lastNav.getNav(), 4, RoundingMode.HALF_UP).compareTo(Constant.CROWN_WHOLE_PORTFOLIO_STOP_LOSS_EXCEPTION) > 0 && nav.getNav().divide(lastNav.getNav(), 4, RoundingMode.HALF_UP).compareTo(TradeTask.getWholePortfolioStopLossBar()) <= 0) {
                                log.info("The whole portfolio is reaching stop loss line");
                                this.handleStopLoss(positions, portfolio, "The whole portfolio is reaching stop loss line");
                            }
                        }
                ));
            }
        });
    }

    //    @Scheduled(fixedDelay = 60000L)
    public void updateVWAP() {
        if (isNotTradeTime()) {
            return;
        }
        vwap.update();
    }

    //    @Scheduled(fixedDelay = 1000L)
    public void tracking() {
        if (isNotTradeTime()) {
            return;
        }
        // Fill position
        securityToTrackMap.forEach((securityCode, securityToTrack) -> {
            if (securityToTrack.vol().equals(0L)) {
                var position = trackingService.queryPosition(securityCode);
                if (position != null) {
                    securityToTrackMap.put(securityCode, new SecurityToTrack(securityCode, Long.valueOf(position.vol()), securityToTrack.states()));
                    securityStatesTrackingQueue.put(securityCode, new ConcurrentLinkedQueue<>(securityToTrack.states()));
                }
            }
        });
//        log.info("security to track map: {}, states queue: {}", securityToTrackMap, securityStatesTrackingQueue);
        var codes = securityToTrackMap.values().stream().filter(securityToTrack -> securityToTrack.vol() > 0).map(SecurityToTrack::stockCode).toList();
        if (codes.isEmpty()) {
            return;
        }
        marketDataClient.getBidAsk(codes).forEach(bidAskBrokerDTO -> {
            var queue = securityStatesTrackingQueue.get(bidAskBrokerDTO.securityCode());
            log.info("top : {}", queue.peek());
            if (queue.isEmpty()) {
                return;
            }
            if (queue.peek().isSellable(bidAskBrokerDTO)) {
                //sell
                var portfolio = portfolioService.getPortfolio("测试");
                var position = new Position();
                position.setSecurityCode(bidAskBrokerDTO.securityCode());
                position.setSecurityShare(securityToTrackMap.get(bidAskBrokerDTO.securityCode()).vol());
                List<OrderDTO> orders = orderService.sell(List.of(position));
                orders.forEach(order -> {
                    log.warn("BidAsk hit: {}", bidAskBrokerDTO);
                    orderService.addOrder(order, portfolio, LocalDateTime.now(), LocalDateTime.now().plusMinutes(1L));
                });
                queue.clear();
            }
            if (queue.isEmpty()) {
                return;
            }
            if (queue.peek().isUpgradable(bidAskBrokerDTO)) {
                queue.poll();
            }
        });
    }

    private boolean isBetween(LocalDateTime startTime, LocalDateTime endTime) {
        LocalDateTime now = LocalDateTime.now();
        return !now.isBefore(startTime) && !now.isAfter(endTime);
    }

    private boolean isNotTradeTime() {
        LocalDateTime now = LocalDateTime.now();
        return now.toLocalTime().isBefore(LocalTime.of(9, 30, 30)) || now.toLocalTime().isAfter(LocalTime.of(14, 50, 0))
                || now.toLocalDate().getDayOfWeek().equals(DayOfWeek.SATURDAY) || now.toLocalDate().getDayOfWeek().equals(DayOfWeek.SUNDAY);
//        return !now.toLocalTime().isBefore(LocalTime.of(9, 30, 30)) && !now.toLocalTime().isAfter(LocalTime.of(14, 50, 0));
    }

    public static boolean isOrderTime() {
        LocalTime now = LocalDateTime.now().toLocalTime();
        return !now.isBefore(LocalTime.of(9, 26, 0)) && !now.isAfter(LocalTime.of(23, 50, 0));
    }

    public static BigDecimal getStopLossBar() {
//        LocalTime now = LocalDateTime.now().toLocalTime();
//        if (!now.isBefore(LocalTime.of(9, 30, 30)) && !now.isAfter(LocalTime.of(10, 30, 30))) {
//            return new BigDecimal("0.97");
//        }
//        return Constant.CROWN_STOP_LOSS;
        return Constant.CROWN_STOP_LOSS_NEW;
    }

    public static BigDecimal getWholePortfolioStopLossBar() {
        LocalTime now = LocalDateTime.now().toLocalTime();
        if (!now.isBefore(LocalTime.of(9, 30, 30)) && !now.isAfter(LocalTime.of(14, 5, 30))) {
            return Constant.CROWN_WHOLE_PORTFOLIO_STOP_LOSS;
        }
        return new BigDecimal("0.985");
    }

    public boolean isSlump(BidAskBrokerDTO bidAskBrokerDTO) {
        if (cbSellStrategyMapping.containsKey(bidAskBrokerDTO.securityCode())) {
            var strategy = cbSellStrategyMapping.get(bidAskBrokerDTO.securityCode());
            return strategy.isSlump(bidAskBrokerDTO);
        }
        return false;
    }

    public boolean isSellable(BidAskBrokerDTO bidAskBrokerDTO) {
        log.info("price: {}", bidAskBrokerDTO);
        if (cbSellStrategyMapping.containsKey(bidAskBrokerDTO.securityCode())) {
            var strategy = cbSellStrategyMapping.get(bidAskBrokerDTO.securityCode());
            strategy.updateState(bidAskBrokerDTO);
        } else {
            CrownSellStrategy strategy = new CrownSellStrategy(marketDataClient, cbStockMappingRepo, vwap);
            strategy.updateState(bidAskBrokerDTO);
            cbSellStrategyMapping.put(bidAskBrokerDTO.securityCode(), strategy);
        }
        log.info("strategy map: {}", cbSellStrategyMapping.get(bidAskBrokerDTO.securityCode()));
        return cbSellStrategyMapping.get(bidAskBrokerDTO.securityCode()).isSellable();
    }

    public void handleStopLossMultiTier(Position position, PositionSnapshot positionSnapshot, Portfolio portfolio, BidAskBrokerDTO bidAskBrokerDTO, String notificationTitle) {
        BigDecimal tier1 = new BigDecimal("0.75");
        BigDecimal tier2 = new BigDecimal("0.5");
        BigDecimal tier3 = new BigDecimal("0.25");
        BigDecimal priceTier2 = new BigDecimal("0.9725");
        BigDecimal priceTier3 = new BigDecimal("0.9675");
        BigDecimal priceTier4 = new BigDecimal("0.9625");
        if (BigDecimal.valueOf(position.getSecurityShare()).compareTo(BigDecimal.valueOf(positionSnapshot.getSecurityShare()).multiply(tier1)) > 0
        ) {
            OrderDTO order = orderService.sell(position, positionSnapshot, tier1.doubleValue());
            if (order.share() > 0) {
                log.info("order: {}", order);
                this.placeOrderWithoutMemoryLock(order, portfolio, notificationTitle);
            }
            return;
        }
        if (BigDecimal.valueOf(position.getSecurityShare()).compareTo(BigDecimal.valueOf(positionSnapshot.getSecurityShare()).multiply(tier2)) > 0
                && Util.priceMovementDivide(bidAskBrokerDTO.askPrice1(), bidAskBrokerDTO.lastClose()).compareTo(priceTier2) <= 0) {
            OrderDTO order = orderService.sell(position, positionSnapshot, tier2.doubleValue());
            if (order.share() > 0) {
                log.info("order: {}", order);
                this.placeOrderWithoutMemoryLock(order, portfolio, notificationTitle);
            }
            return;
        }
        if (BigDecimal.valueOf(position.getSecurityShare()).compareTo(BigDecimal.valueOf(positionSnapshot.getSecurityShare()).multiply(tier3)) > 0
                && Util.priceMovementDivide(bidAskBrokerDTO.askPrice1(), bidAskBrokerDTO.lastClose()).compareTo(priceTier3) <= 0) {
            OrderDTO order = orderService.sell(position, positionSnapshot, tier3.doubleValue());
            if (order.share() > 0) {
                log.info("order: {}", order);
                this.placeOrderWithoutMemoryLock(order, portfolio, notificationTitle);
            }
            return;
        }
        if (Util.priceMovementDivide(bidAskBrokerDTO.askPrice1(), bidAskBrokerDTO.lastClose()).compareTo(priceTier4) <= 0) {
            OrderDTO order = orderService.sell(position);
            this.placeOrderWithoutMemoryLock(order, portfolio, notificationTitle);
        }
    }

    private void handleStopLoss(List<Position> selectedPositions, Portfolio portfolio, String notificationTitle) {
        List<OrderDTO> orders = orderService.sell(selectedPositions);
        orders.forEach(order -> this.placeOrder(order, portfolio, notificationTitle));
    }

    private void placeOrderWithoutMemoryLock(OrderDTO order, Portfolio portfolio, String notificationTitle) {
        orderService.addOrder(order, portfolio, LocalDateTime.now(), LocalDateTime.now().plusMinutes(1L));
        wechatPublicAccount.send(notificationTitle, order.toString());
        positionBookForCrownRepo.findByPortfolioNameAndSecurityCode(portfolio.getName(), order.securityCode()).ifPresent(book -> {
            book.setSellLock(true);
            positionBookForCrownRepo.save(book);
        });
    }

    private void placeOrder(OrderDTO order, Portfolio portfolio, String notificationTitle) {
        if (!sellLockSet.contains(order.securityCode())) {
            orderService.addOrder(order, portfolio, LocalDateTime.now(), LocalDateTime.now().plusMinutes(1L));
            wechatPublicAccount.send(notificationTitle, order.toString());
            sellLockSet.add(order.securityCode());
            positionBookForCrownRepo.findByPortfolioNameAndSecurityCode(portfolio.getName(), order.securityCode()).ifPresent(book -> {
                book.setSellLock(true);
//                    book.setBuyLock(true);
                positionBookForCrownRepo.save(book);
            });
        }
    }

}
