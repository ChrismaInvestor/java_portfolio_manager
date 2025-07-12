package com.portfolio.manager.integration;

import com.portfolio.manager.domain.Price;
import com.portfolio.manager.domain.strategy_specific.CbStockMapping;
import com.portfolio.manager.dto.integration.BidAskBrokerDTO;
import com.portfolio.manager.dto.integration.SecurityInfoDTO;

import java.util.List;

public interface MarketDataClient {
    List<SecurityInfoDTO> listAllStocksInfo();

    List<CbStockMapping> listCbStockMapping();

    List<BidAskBrokerDTO> getBidAsk(List<String> securityCodes);

    List<Price> listMinPrice(List<String> securityCodes);
}
