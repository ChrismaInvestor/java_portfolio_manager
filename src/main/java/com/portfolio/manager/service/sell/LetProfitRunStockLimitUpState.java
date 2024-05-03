package com.portfolio.manager.service.sell;

import com.portfolio.manager.domain.strategy_specific.CbStockMapping;
import com.portfolio.manager.dto.BidAskBrokerDTO;
import com.portfolio.manager.integration.MarketDataClient;
import com.portfolio.manager.repository.CbStockMappingRepo;

import java.util.List;
import java.util.Optional;

public class LetProfitRunStockLimitUpState extends State{
    CrownSellStrategy crownSellStrategy;

    MarketDataClient marketDataClient;

    CbStockMappingRepo cbStockMappingRepo;

    public LetProfitRunStockLimitUpState(CrownSellStrategy crownSellStrategy, MarketDataClient marketDataClient, CbStockMappingRepo cbStockMappingRepo){
        this.crownSellStrategy = crownSellStrategy;
        this.marketDataClient = marketDataClient;
        this.cbStockMappingRepo = cbStockMappingRepo;
    }
    @Override
    public void updateState(BidAskBrokerDTO bidAskBrokerDTO) {
        Optional<CbStockMapping> mapping = cbStockMappingRepo.findByCbCode(bidAskBrokerDTO.securityCode());
        mapping.ifPresent(v -> {
            Optional<BidAskBrokerDTO> bidAsk = marketDataClient.getBidAsk(List.of(v.getStockCode())).stream().findFirst();
            bidAsk.ifPresent(bidAskBroker -> {
                if (bidAskBroker.askVol2() > 0) {
                    crownSellStrategy.setState(crownSellStrategy.stopLossState);
                }
            });
        });
    }

    @Override
    public boolean isSellable() {
        return false;
    }

}
