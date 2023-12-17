package com.portfolio.manager.web;

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
        if (portfolioService.getPortfolio(orderPlacement.portfolio()).getTakeProfitStopLoss()){
            positionBookForCrownRepo.deleteByPortfolioName(orderPlacement.portfolio());
            List<PositionBookForCrown> positionBook = orderPlacement.orders().stream().map(orderDTO -> {
                PositionBookForCrown positionBookForCrown = new PositionBookForCrown();
                positionBookForCrown.setPortfolioName(orderPlacement.portfolio());
                positionBookForCrown.setSecurityCode(orderDTO.securityCode());
                positionBookForCrown.setSecurityShare(orderDTO.share());
                positionBookForCrown.setSecurityName(orderDTO.securityName());
                return positionBookForCrown;
            }).toList();
            positionBookForCrownRepo.saveAll(positionBook);
        }

    }

    @GetMapping("order")
    public List<OrderInProgressDTO> listOrders(@RequestParam(name = "currentPortfolio") String portfolio) {
        return orderService.listOrdersInProgress(portfolio);
    }
}
