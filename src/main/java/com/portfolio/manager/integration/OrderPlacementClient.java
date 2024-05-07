package com.portfolio.manager.integration;

import com.portfolio.manager.dto.AccountDTO;
import com.portfolio.manager.dto.CancelableOrderDTO;
import com.portfolio.manager.dto.PositionIntegrateDTO;
import com.portfolio.manager.dto.TradeDTO;

import java.util.List;

public interface OrderPlacementClient {

    // 返回orderId
    String buy(String code, Double price, Integer vol);

    // 返回orderId
    String sell(String code, Double price, Integer vol);

    @Deprecated
    PositionIntegrateDTO checkPosition(String code);

    List<PositionIntegrateDTO> queryAllPositions();

    List<TradeDTO> listTodayTrades();

    AccountDTO queryAcct();

    List<CancelableOrderDTO> queryCancelableOrders();

    Boolean cancelOrder(Long orderId);
}
