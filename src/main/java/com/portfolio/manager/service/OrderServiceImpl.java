package com.portfolio.manager.service;

import com.portfolio.manager.constant.Constant;
import com.portfolio.manager.domain.*;
import com.portfolio.manager.dto.OrderDTO;
import com.portfolio.manager.dto.OrderInProgressDTO;
import com.portfolio.manager.repository.OrderRepo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OrderServiceImpl implements OrderService {
    @Resource
    private PriceService priceService;

    @Resource
    private SecurityService securityService;

    @Resource
    private OrderRepo orderRepo;

    @Resource
    private AlgoService algoService;


    @Override
    public List<OrderDTO> buySplitEven(Set<String> securityCodes, double toSellMarketValue, double cash, List<Position> holdings) {
        log.info("new codes: {}, holding codes: {}", securityCodes, holdings);
        double holdingsValue = holdings.stream().mapToDouble(holding ->
                {
                    try {
                        return BigDecimal.valueOf(priceService.getLatestPrice(holding.getSecurityCode())).multiply(BigDecimal.valueOf(holding.getSecurityShare())).doubleValue();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
        ).sum();
        BigDecimal totalValue = BigDecimal.valueOf(toSellMarketValue).add(BigDecimal.valueOf(cash)).add(BigDecimal.valueOf(holdingsValue));
        final BigDecimal average = totalValue.divide(BigDecimal.valueOf(securityCodes.size()), RoundingMode.HALF_DOWN).setScale(2, RoundingMode.HALF_DOWN);
        List<BigDecimal> maxTotal = new ArrayList<>();
        List<BigDecimal> minTotal = new ArrayList<>();
        List<OrderDTO> orders = new ArrayList<>();
        Map<String, Position> holdingCodes = holdings.stream().collect(Collectors.toMap(Position::getSecurityCode, Function.identity()));
        securityCodes.forEach(code -> {
            long multiple = code.startsWith("11") || code.startsWith("12") ? Constant.CONVERTIBLE_BOND_MULTIPLE : Constant.STOCK_MULTIPLE;
            String internalCode = code.split("\\.")[0];
            BigDecimal price = null;
            try {
                price = BigDecimal.valueOf(priceService.getLatestPrice(internalCode));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            BigDecimal divide = average.divide(price.multiply(BigDecimal.valueOf(multiple)), RoundingMode.HALF_EVEN);
            BigDecimal min = divide.setScale(0, RoundingMode.DOWN);
//            BigDecimal max = divide.setScale(0, RoundingMode.UP);
//            maxTotal.add(max.multiply(price).multiply(BigDecimal.valueOf(multiple)));
//            minTotal.add(min.multiply(price).multiply(BigDecimal.valueOf(multiple)));
            if (holdingCodes.containsKey(internalCode)) {
                min = min.subtract(BigDecimal.valueOf(holdingCodes.get(internalCode).getSecurityShare()).divide(BigDecimal.valueOf(multiple), RoundingMode.HALF_EVEN));
                if (min.compareTo(BigDecimal.ZERO) < 0) {
                    OrderDTO order = new OrderDTO(Direction.卖出, min.abs().multiply(BigDecimal.valueOf(multiple)).longValue(), securityService.getSecurityName(code.split("\\.")[0]), code, min.abs().multiply(price).multiply(BigDecimal.valueOf(multiple)).doubleValue());
                    orders.add(order);
                } else {
                    OrderDTO order = new OrderDTO(Direction.买入, min.multiply(BigDecimal.valueOf(multiple)).longValue(), securityService.getSecurityName(code.split("\\.")[0]), code, min.multiply(price).multiply(BigDecimal.valueOf(multiple)).doubleValue());
                    orders.add(order);
                }
            } else {
                OrderDTO order = new OrderDTO(Direction.买入, min.multiply(BigDecimal.valueOf(multiple)).longValue(), securityService.getSecurityName(code.split("\\.")[0]), code, min.multiply(price).multiply(BigDecimal.valueOf(multiple)).doubleValue());
                orders.add(order);
            }
        });
//        log.info("Max total: {}, min total: {}", maxTotal.stream().mapToDouble(BigDecimal::doubleValue).sum(), minTotal.stream().mapToDouble(BigDecimal::doubleValue).sum());
        return orders.stream().filter(order -> order.share() > 0).toList();
    }

    @Override
    public List<OrderDTO> buySplitEvenV2(Set<String> securityCodes, double cash, List<Position> holdings) {
        double holdingsValue = holdings.stream().mapToDouble(holding ->
                {
                    try {
                        return BigDecimal.valueOf(priceService.getLatestPrice(holding.getSecurityCode())).multiply(BigDecimal.valueOf(holding.getSecurityShare())).doubleValue();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
        ).sum();
        return null;
    }

    @Override
    public List<OrderDTO> sell(List<Position> toSell) {
        return toSell.stream().map(position -> {
            try {
                return new OrderDTO(Direction.卖出, position.getSecurityShare(), securityService.getSecurityName(position.getSecurityCode()), position.getSecurityCode(), BigDecimal.valueOf(priceService.getLatestPrice(position.getSecurityCode())).multiply(BigDecimal.valueOf(position.getSecurityShare())).doubleValue());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).toList();
    }

    @Override
    public void addOrder(OrderDTO orderDTO, String portfolio, LocalDateTime startTime, LocalDateTime endTime) {
        Order order = new Order();
        order.setPlannedShare(orderDTO.share());
        order.setRemainingShare(orderDTO.share());
        order.setSecurityCode(orderDTO.securityCode().split("\\.")[0]);
        order.setPortfolioName(portfolio);
        order.setBuyOrSell(orderDTO.buyOrSell());
        order.setSubOrders(algoService.testSplitOrders(order, startTime, endTime));
        orderRepo.save(order);
    }

    @Override
    public List<OrderInProgressDTO> listOrdersInProgress(String portfolio) {
        List<Order> rawOrders = orderRepo.findByPortfolioName(portfolio).stream().filter(order -> order.getRemainingShare() > 0L).toList();
        return rawOrders.stream().map(rawOrder -> {
            BigDecimal plannedShare = BigDecimal.valueOf(rawOrder.getPlannedShare());
            long multiple = rawOrder.getSecurityCode().startsWith("11") || rawOrder.getSecurityCode().startsWith("12") ? Constant.CONVERTIBLE_BOND_MULTIPLE : Constant.STOCK_MULTIPLE;
            int ratio = BigDecimal.valueOf(rawOrder.getPlannedShare() - rawOrder.getRemainingShare()).divide(plannedShare, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(multiple)).intValue();
            return new OrderInProgressDTO(rawOrder.getBuyOrSell().name(), securityService.getSecurityName(rawOrder.getSecurityCode()), rawOrder.getSecurityCode(), ratio);
        }).toList();
    }

    @Override
    public List<Order> listOrders(String portfolio) {
        return orderRepo.findByPortfolioName(portfolio);
    }

    @Override
    public List<Order> listPendingOrders(String portfolio) {
        return orderRepo.findByPortfolioName(portfolio).stream().filter(order -> order.getRemainingShare() > 0).toList();
    }

    @Override
    public void updateOrders(Portfolio portfolio) {
        List<Order> ordersToUpdate = this.listOrders(portfolio.getName());
        ordersToUpdate.forEach(order -> {
            long sum = order.getSubOrders().stream().mapToLong(SubOrder::getRemainingShare).sum();
            order.setRemainingShare(sum);
        });
        orderRepo.saveAll(ordersToUpdate);
    }

    @Override
    public List<OrderDTO> generateOrder(Map<String, Integer> securityToWeight, BigDecimal cash) {
        final int totalWeight = securityToWeight.values().stream().mapToInt(weight -> weight).sum();
        final BigDecimal unitCash = cash.divide(BigDecimal.valueOf(totalWeight), RoundingMode.HALF_DOWN).setScale(2, RoundingMode.HALF_DOWN);
        List<OrderDTO> orders = new ArrayList<>();
        securityToWeight.forEach((securityCode, weight)->{
            BigDecimal perCash = unitCash.multiply(BigDecimal.valueOf(weight)).setScale(2, RoundingMode.HALF_DOWN);
            long multiple = securityCode.startsWith("11") || securityCode.startsWith("12") ? Constant.CONVERTIBLE_BOND_MULTIPLE : Constant.STOCK_MULTIPLE;
            String internalCode = securityCode.split("\\.")[0];
            BigDecimal price = null;
            try {
                price = BigDecimal.valueOf(priceService.getLatestPrice(internalCode));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            BigDecimal volWithoutMultiple = perCash.divide(price.multiply(BigDecimal.valueOf(multiple)), RoundingMode.HALF_EVEN).setScale(0, RoundingMode.DOWN);
            OrderDTO order = new OrderDTO(Direction.买入, volWithoutMultiple.multiply(BigDecimal.valueOf(multiple)).longValue(), securityService.getSecurityName(securityCode.split("\\.")[0]), securityCode, volWithoutMultiple.multiply(price).multiply(BigDecimal.valueOf(multiple)).doubleValue());
            orders.add(order);
        } );
        return orders;
    }

}
