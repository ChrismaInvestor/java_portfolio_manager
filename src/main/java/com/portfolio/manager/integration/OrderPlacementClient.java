package com.portfolio.manager.integration;

import com.portfolio.manager.dto.CancelableOrderDTO;
import com.portfolio.manager.dto.PositionIntegrateDTO;
import com.portfolio.manager.dto.TradeDTO;

import java.math.BigDecimal;
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

    BigDecimal queryCash();

    List<CancelableOrderDTO> queryCancelableOrders();

    Boolean cancelOrder(Long orderId);
}
