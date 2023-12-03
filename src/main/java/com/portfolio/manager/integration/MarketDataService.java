package com.portfolio.manager.integration;

import com.portfolio.manager.domain.Price;
import com.portfolio.manager.dto.BidAskBrokerDTO;
import com.portfolio.manager.dto.SecurityDTO;

import java.util.List;

public interface MarketDataService {
    List<SecurityDTO> listAllStocksInfo();

    List<Price> listMinPrice(String code);

    List<BidAskBrokerDTO> getBidAsk(List<String> securityCodes);
}
