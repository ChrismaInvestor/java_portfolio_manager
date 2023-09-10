package com.portfolio.manager.web;

import com.portfolio.manager.domain.Position;
import com.portfolio.manager.dto.OrderDTO;
import com.portfolio.manager.dto.OrderPlacementDTO;
import com.portfolio.manager.dto.PositionDTO;
import com.portfolio.manager.dto.SecurityDTO;
import com.portfolio.manager.service.PositionService;
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
    PositionService positionService;

    @PostMapping
    public List<OrderDTO> addPosition(@RequestBody PositionDTO positionDTO) {
        List<Position> oldPositions = positionService.listPosition(positionDTO.portfolio());
        Set<String> oldPositionCodes = oldPositions.stream().map(Position::getSecurityCode).collect(Collectors.toSet());
        Set<String> newPositionCodes = positionDTO.positions().stream().map(SecurityDTO::code).collect(Collectors.toSet());
        Set<String> intersection = new HashSet<>(oldPositionCodes);
        intersection.retainAll(newPositionCodes);
        oldPositionCodes.removeAll(newPositionCodes);
        List<Position> toSell = oldPositions.stream().filter(oldPosition -> oldPositionCodes.contains(oldPosition.getSecurityCode())).toList();
        List<OrderDTO> sellOrder = positionService.sell(toSell);
        double toSellMarketValue = sellOrder.stream().mapToDouble(OrderDTO::value).sum();
        double cash = positionService.getCash(positionDTO.portfolio());
        List<OrderDTO> buyOrder = positionService.buySplitEven(newPositionCodes, toSellMarketValue, cash, oldPositions.stream().filter(position -> intersection.contains(position.getSecurityCode())).toList());
        return Stream.concat(sellOrder.stream(), buyOrder.stream()).toList();
    }

    @PostMapping("order")
    public void addOrders(@RequestBody OrderPlacementDTO orderPlacement){
        log.info("{}", orderPlacement);
    }
}
