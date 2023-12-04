package com.portfolio.manager.service;

import com.portfolio.manager.integration.MarketDataService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class PriceServiceImpl implements PriceService {

    @Resource
    MarketDataService marketDataService;

    @Override
    public Double getLatestPrice(String code) {
        return marketDataService.getBidAsk(List.of(code)).get(0).lastPrice();
    }

}
