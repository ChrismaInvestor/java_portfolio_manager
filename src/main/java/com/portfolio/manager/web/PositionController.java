package com.portfolio.manager.web;

import com.portfolio.manager.domain.Direction;
import com.portfolio.manager.domain.Portfolio;
import com.portfolio.manager.domain.Position;
import com.portfolio.manager.domain.strategy_specific.PositionBookForCrown;
import com.portfolio.manager.dto.ui.OrderDTO;
import com.portfolio.manager.dto.ui.OrderInProgressDTO;
import com.portfolio.manager.dto.ui.OrderPlacementDTO;
import com.portfolio.manager.dto.ui.PositionDTO;
import com.portfolio.manager.notification.Notification;
import com.portfolio.manager.repository.PositionBookForCrownRepo;
import com.portfolio.manager.service.OrderService;
import com.portfolio.manager.service.PortfolioService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@RestController
@RequestMapping("/position")
@CrossOrigin
public class PositionController {

    @Resource
    PortfolioService portfolioService;

    @Resource
    OrderService orderService;

    @Resource
    PositionBookForCrownRepo positionBookForCrownRepo;

    @Resource
    @Qualifier("WechatPublicAccount")
    Notification wechatPublicAccount;

    //    Temporary
    @GetMapping("clear")
    public String clear() {
        LocalDateTime time = LocalDate.now().atTime(14, 50, 0);
        portfolioService.listPortfolio().forEach(portfolio -> {
            List<PositionBookForCrown> positionBookForCrownList = positionBookForCrownRepo.findByPortfolioName(portfolio.getName());
            positionBookForCrownList.forEach(positionBookForCrown -> positionBookForCrown.setBuyBack(false));
            positionBookForCrownRepo.saveAll(positionBookForCrownList);
            portfolioService.listPosition(portfolio.getName()).forEach(position -> {
                OrderDTO orderDTO = new OrderDTO(Direction.卖出, position.getSecurityShare(), "", position.getSecurityCode(), 0.0d);
                orderService.addOrder(orderDTO, portfolio, time, time.plusMinutes(6L));
                wechatPublicAccount.send("Portfolio clear at weekly last trading day", position.getSecurityCode());
            });
        });
        return "succeed";
    }

    @PostMapping
    public List<OrderDTO> calOrdersWithWeights(@RequestBody PositionDTO positionDTO) {
        var portfolio = portfolioService.getPortfolio(positionDTO.portfolio());
        BigDecimal currentTotalMarketValue = BigDecimal.valueOf(portfolioService.getDynamics(portfolio).getTotalMarketValue());
        int positionSize = positionDTO.positions().size();
        Map<String, BigDecimal> securityCodeTargetPositionMap = new HashMap<>();
        positionDTO.positions().forEach(securityInfoDTO -> {
            BigDecimal weight = securityInfoDTO.weight() == null ? BigDecimal.ONE.divide(BigDecimal.valueOf(positionSize), 6, RoundingMode.HALF_EVEN) : new BigDecimal(securityInfoDTO.weight());
            BigDecimal targetPosition = currentTotalMarketValue.multiply(weight);
            String internalSecurityCode = securityInfoDTO.code().split("\\.")[0];
            securityCodeTargetPositionMap.put(internalSecurityCode, targetPosition);
        });
        List<Position> currentPositions = portfolioService.listPosition(positionDTO.portfolio());
        Map<String, Position> currentPositionsMap = currentPositions.stream().collect(Collectors.toMap(Position::getSecurityCode, Function.identity()));
        List<OrderDTO> buyOrders = new ArrayList<>();
        securityCodeTargetPositionMap.forEach((securityCode, targetPosition) -> {
            log.info("security code: {}, currentPosition: {}", securityCode, currentPositionsMap.getOrDefault(securityCode, null));
            var order = orderService.buy(securityCode, targetPosition, currentPositionsMap.getOrDefault(securityCode, null));
            buyOrders.add(order);
        });
        List<Position> toSell = currentPositions.stream().filter(currentPosition -> !securityCodeTargetPositionMap.containsKey(currentPosition.getSecurityCode())).toList();
        List<OrderDTO> sellOrder = orderService.sell(toSell);
        return Stream.concat(sellOrder.stream(), buyOrders.stream()).toList();
    }

//    @PostMapping
//    public List<OrderDTO> calOrders(@RequestBody PositionDTO positionDTO) {
//        var portfolio = portfolioService.getPortfolio(positionDTO.portfolio());
//        log.info("total market value: {}", portfolioService.getDynamics(portfolio).getTotalMarketValue());
//        List<Position> oldPositions = portfolioService.listPosition(positionDTO.portfolio());
//        Set<String> oldPositionCodes = oldPositions.stream().map(Position::getSecurityCode).collect(Collectors.toSet());
//        Set<String> newPositionCodes = positionDTO.positions().stream().map(p -> p.code().split("\\.")[0]).collect(Collectors.toSet());
//        Set<String> intersection = new HashSet<>(oldPositionCodes);
//        intersection.retainAll(newPositionCodes);
//        oldPositionCodes.removeAll(newPositionCodes);
//        List<Position> toSell = oldPositions.stream().filter(oldPosition -> oldPositionCodes.contains(oldPosition.getSecurityCode())).toList();
//        List<OrderDTO> sellOrder = orderService.sell(toSell);
//        double toSellMarketValue = sellOrder.stream().mapToDouble(OrderDTO::value).sum();
//        double cash = portfolioService.getCash(positionDTO.portfolio());
//        List<OrderDTO> buyOrder = orderService.buySplitEven(newPositionCodes, toSellMarketValue, cash, oldPositions.stream().filter(position -> intersection.contains(position.getSecurityCode())).toList());
//        log.info("buy total: {}", buyOrder.stream().mapToDouble(OrderDTO::value).sum());
//        return Stream.concat(sellOrder.stream(), buyOrder.stream()).toList();
//    }

    @PostMapping("order")
    public void addOrders(@RequestBody OrderPlacementDTO orderPlacement) {
        log.info("{}", orderPlacement);
//        positionSnapshotService.update(portfolioService.listPosition(orderPlacement.portfolio()), orderPlacement.orders());
        Portfolio portfolio = portfolioService.getPortfolio(orderPlacement.portfolio());
        orderPlacement.orders().forEach(orderDTO -> orderService.addOrder(orderDTO, portfolio, orderPlacement.startTime().plusHours(8L), orderPlacement.endTime().plusHours(8L)));
        //For crown strategy only
        if (portfolioService.getPortfolio(orderPlacement.portfolio()).getTakeProfitStopLoss()) {
            orderPlacement.orders().forEach(order -> positionBookForCrownRepo.findByPortfolioNameAndSecurityCode(orderPlacement.portfolio(), order.securityCode()).ifPresentOrElse(
                    p -> {
                        Optional<Position> currentPosition = portfolioService.listPosition(orderPlacement.portfolio()).stream().filter(currentP -> currentP.getSecurityCode().equals(order.securityCode())).findFirst();
                        if (order.buyOrSell().equals(Direction.买入)) {
                            currentPosition.ifPresentOrElse(currentP -> p.setSecurityShare(currentP.getSecurityShare() + order.share()), () -> p.setSecurityShare(order.share()));
                            LocalDateTime orderStartTime = orderPlacement.startTime().plusHours(8L);
                            if (!orderStartTime.isBefore(orderStartTime.toLocalDate().atTime(2, 50, 0)) && ChronoUnit.DAYS.between(orderStartTime.toLocalDate(), LocalDate.now()) == 0) {
                                p.setBuyBack(false);
                            }
                            positionBookForCrownRepo.save(p);
                        } else if (order.buyOrSell().equals(Direction.卖出)) {
                            currentPosition.ifPresent(currentP -> {
                                p.setSecurityShare(currentP.getSecurityShare() - order.share());
                                if (p.getSecurityShare().compareTo(0L) <= 0) {
                                    positionBookForCrownRepo.delete(p);
                                } else {
                                    positionBookForCrownRepo.save(p);
                                }
                            });
                        }
                    }
                    , () -> {
                        PositionBookForCrown positionBookForCrown = new PositionBookForCrown();
                        positionBookForCrown.setPortfolioName(orderPlacement.portfolio());
                        positionBookForCrown.setSecurityCode(order.securityCode());
                        positionBookForCrown.setSecurityShare(order.share());
                        positionBookForCrown.setSecurityName(order.securityName());
                        positionBookForCrown.setSellLock(false);
                        positionBookForCrownRepo.save(positionBookForCrown);
                    }));
        }

    }

    @GetMapping("order")
    public List<OrderInProgressDTO> listOrders(@RequestParam(name = "currentPortfolio") String portfolio) {
        return orderService.listOrdersInProgress(portfolio);
    }
}
