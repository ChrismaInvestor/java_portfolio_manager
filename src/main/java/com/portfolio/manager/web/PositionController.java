package com.portfolio.manager.web;

import com.portfolio.manager.domain.Direction;
import com.portfolio.manager.domain.Position;
import com.portfolio.manager.domain.strategy_specific.PositionBookForCrown;
import com.portfolio.manager.dto.*;
import com.portfolio.manager.repository.PositionBookForCrownRepo;
import com.portfolio.manager.service.OrderService;
import com.portfolio.manager.service.PortfolioService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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

    @PostMapping
    public List<OrderDTO> calOrders(@RequestBody PositionDTO positionDTO) {
        List<Position> oldPositions = portfolioService.listPosition(positionDTO.portfolio());
        Set<String> oldPositionCodes = oldPositions.stream().map(Position::getSecurityCode).collect(Collectors.toSet());
        Set<String> newPositionCodes = positionDTO.positions().stream().map(p -> p.code().split("\\.")[0]).collect(Collectors.toSet());
        Set<String> intersection = new HashSet<>(oldPositionCodes);
        intersection.retainAll(newPositionCodes);
        oldPositionCodes.removeAll(newPositionCodes);
        List<Position> toSell = oldPositions.stream().filter(oldPosition -> oldPositionCodes.contains(oldPosition.getSecurityCode())).toList();
        List<OrderDTO> sellOrder = orderService.sell(toSell);
        double toSellMarketValue = sellOrder.stream().mapToDouble(OrderDTO::value).sum();
        double cash = portfolioService.getCash(positionDTO.portfolio());
        List<OrderDTO> buyOrder = orderService.buySplitEven(newPositionCodes, toSellMarketValue, cash, oldPositions.stream().filter(position -> intersection.contains(position.getSecurityCode())).toList());
        log.info("buy total: {}", buyOrder.stream().mapToDouble(OrderDTO::value).sum());
        return Stream.concat(sellOrder.stream(), buyOrder.stream()).toList();
    }

    @PostMapping("order")
    public void addOrders(@RequestBody OrderPlacementDTO orderPlacement) {
        log.info("{}", orderPlacement);
        orderPlacement.orders().stream().parallel().forEach(orderDTO -> orderService.addOrder(orderDTO, orderPlacement.portfolio(), orderPlacement.startTime().plusHours(8L), orderPlacement.endTime().plusHours(8L)));
        //For crown strategy only
        if (portfolioService.getPortfolio(orderPlacement.portfolio()).getTakeProfitStopLoss()) {
            orderPlacement.orders().forEach(order -> positionBookForCrownRepo.findByPortfolioNameAndSecurityCode(orderPlacement.portfolio(), order.securityCode()).ifPresentOrElse(
                    p -> {
                        Optional<Position> currentPosition = portfolioService.listPosition(orderPlacement.portfolio()).stream().filter(currentP -> currentP.getSecurityCode().equals(order.securityCode())).findFirst();
                        if (order.buyOrSell().equals(Direction.买入)) {
                            currentPosition.ifPresentOrElse(currentP -> p.setSecurityShare(currentP.getSecurityShare() + order.share()),()-> p.setSecurityShare(order.share()));
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
            Set<String> codesOfOrders = orderPlacement.orders().stream().map(OrderDTO::securityCode).collect(Collectors.toSet());
            positionBookForCrownRepo.findByPortfolioName(orderPlacement.portfolio()).forEach(p->{
                if (!codesOfOrders.contains(p.getSecurityCode())){
                    positionBookForCrownRepo.delete(p);
                }
            });
        }

    }

    @GetMapping("order")
    public List<OrderInProgressDTO> listOrders(@RequestParam(name = "currentPortfolio") String portfolio) {
        return orderService.listOrdersInProgress(portfolio);
    }
}
