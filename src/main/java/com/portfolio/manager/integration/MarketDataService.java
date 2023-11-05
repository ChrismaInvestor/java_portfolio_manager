package com.portfolio.manager.integration;

import com.portfolio.manager.dto.SecurityDTO;

import java.util.List;

public interface MarketDataService {
    List<SecurityDTO> listAllStocksInfo();
}
