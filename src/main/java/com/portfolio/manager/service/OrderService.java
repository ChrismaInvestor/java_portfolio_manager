package com.portfolio.manager.service;

import com.portfolio.manager.domain.Order;
import com.portfolio.manager.domain.Portfolio;
import com.portfolio.manager.domain.Position;
import com.portfolio.manager.dto.OrderDTO;
import com.portfolio.manager.dto.OrderInProgressDTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public interface OrderService {
    //Estimation
    List<OrderDTO> buySplitEven(Set<String> securityCodes, double toSellMarketValue, double cash, List<Position> holdings);

    //Estimation
    List<OrderDTO> sell(List<Position> toSell);

    void addOrder(OrderDTO orderDTO, String portfolio, LocalDateTime startTime, LocalDateTime endTime);

    List<OrderInProgressDTO> listOrdersInProgress(String portfolio);

    List<Order> listOrders(String portfolio);

    void updateOrders(Portfolio portfolio);

}
