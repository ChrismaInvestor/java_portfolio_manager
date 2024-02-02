package com.portfolio.manager.integration;

import com.portfolio.manager.dto.PositionIntegrateDTO;
import com.portfolio.manager.dto.TradeDTO;

import java.util.List;

public interface OrderPlacementClient {

    // 返回orderId
    String buy(String code, Double price, Integer vol);

    // 返回orderId
    String sell(String code, Double price, Integer vol);

    PositionIntegrateDTO checkPosition(String code);

    List<TradeDTO> listTodayTrades();

}
