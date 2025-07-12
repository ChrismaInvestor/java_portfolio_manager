package com.portfolio.manager.integration;

import com.portfolio.manager.dto.integration.AccountDTO;
import com.portfolio.manager.dto.integration.CancelableOrderDTO;
import com.portfolio.manager.dto.integration.PositionBrokerDTO;
import com.portfolio.manager.dto.integration.TradeDTO;

import java.util.List;

public interface OrderPlacementClient {

    // 返回orderId
    String buy(String code, Double price, Integer vol);

    // 返回orderId
    String sell(String code, Double price, Integer vol);

    List<PositionBrokerDTO> queryAllPositions();

    List<TradeDTO> listTodayTrades();

    AccountDTO queryAcct();

    List<CancelableOrderDTO> queryCancelableOrders();

    Boolean cancelOrder(Long orderId);
}
