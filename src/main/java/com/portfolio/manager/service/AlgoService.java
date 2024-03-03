package com.portfolio.manager.service;

import com.portfolio.manager.domain.Order;
import com.portfolio.manager.domain.SubOrder;

import java.time.LocalDateTime;
import java.util.List;

public interface AlgoService {
    List<SubOrder> splitOrders(Order order, LocalDateTime startTime, LocalDateTime endTime);

    void execute(SubOrder order, Long orderId, Double price, Integer vol);
}
