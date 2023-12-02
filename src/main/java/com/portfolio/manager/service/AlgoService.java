package com.portfolio.manager.service;

import com.portfolio.manager.domain.Order;
import com.portfolio.manager.domain.SubOrder;

import java.time.LocalDateTime;
import java.util.List;

public interface AlgoService {
    List<SubOrder> testSplitOrders(Order order, LocalDateTime startTime);

    void execute(SubOrder order, Long orderId, Double price, Integer vol);
}
