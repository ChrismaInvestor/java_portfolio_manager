package com.portfolio.manager.integration;

import com.portfolio.manager.domain.Price;
import com.portfolio.manager.domain.strategy_specific.CbStockMapping;
import com.portfolio.manager.dto.BidAskBrokerDTO;
import com.portfolio.manager.dto.SecurityDTO;

import java.util.List;

public interface MarketDataClient {
    List<SecurityDTO> listAllStocksInfo();

    List<CbStockMapping> listCbStockMapping();

    List<BidAskBrokerDTO> getBidAsk(List<String> securityCodes);

    List<Price> listMinPrice(List<String> securityCodes);
}
