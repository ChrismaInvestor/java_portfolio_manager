package com.portfolio.manager.data;

import com.portfolio.manager.dto.integration.BidAskBrokerDTO;
import com.portfolio.manager.integration.MarketDataClient;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class BidAskData implements IData{
    List<String> securityCodes = new ArrayList<>();

    List<BidAskBrokerDTO> bidAskList = new ArrayList<>();

    @Resource
    MarketDataClient marketDataClient;

    @Override
    public Map<String, BidAskBrokerDTO> getMap(){
        return bidAskList.stream().collect(Collectors.toMap(BidAskBrokerDTO::securityCode, Function.identity()));
    }

    @Override
    public void update() {
        bidAskList = marketDataClient.getBidAsk(securityCodes.stream().toList());
    }
}
