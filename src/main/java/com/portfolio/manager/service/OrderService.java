package com.portfolio.manager.service;

import com.portfolio.manager.domain.*;
import com.portfolio.manager.dto.ui.OrderDTO;
import com.portfolio.manager.dto.ui.OrderInProgressDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface OrderService {
    //Estimation
    List<OrderDTO> buySplitEven(Set<String> securityCodes, double toSellMarketValue, double cash, List<Position> holdings);

    List<OrderDTO> buySplitEvenV2(Set<String> securityCodes,  double cash, List<Position> holdings);

    OrderDTO buy(String securityCode, BigDecimal targetPosition, Position currentPosition);

    //Estimation
    List<OrderDTO> sell(List<Position> toSell);

    OrderDTO sell(Position toSell);

    OrderDTO sell(Position toSell, PositionSnapshot positionSnapshot, double ratio);

    void addOrder(OrderDTO orderDTO, Portfolio portfolio, LocalDateTime startTime, LocalDateTime endTime);

    List<OrderInProgressDTO> listOrdersInProgress(String portfolio);

    List<Order> listOrders(String portfolio);

    List<Order> listPendingOrders(String portfolio);

    void updateOrders(Portfolio portfolio);

    List<OrderDTO> generateOrderPerWeight(Map<String, BigDecimal> securityToWeight, BigDecimal cash);

//    No Algo, plain trade
    void execute(SubOrder order, Long orderId, Double price, Integer vol);

}
