package com.portfolio.manager.service;

import com.portfolio.manager.integration.MarketDataClient;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
public class PriceServiceImpl implements PriceService {

    @Resource
    MarketDataClient marketDataClient;

    @Override
    public Double getLatestPrice(String code) throws IOException {
        var bidAskList = marketDataClient.getBidAsk(List.of(code));
        if (bidAskList.isEmpty()){
            throw new IOException("Price quote met issues");
        }
        return bidAskList.get(0).askPrice1();
    }

}
