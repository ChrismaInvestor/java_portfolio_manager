package com.portfolio.manager.service.sell;

import com.portfolio.manager.dto.BidAskBrokerDTO;
import com.portfolio.manager.integration.MarketDataClient;
import com.portfolio.manager.repository.CbStockMappingRepo;

public class CrownSellStrategy {
    State letProfitRunState;
    State normalState;
    State stopLossState;
    State lockProfitState;
    State letProfitRunStockLimitUpState;

    State state;

    public CrownSellStrategy(MarketDataClient marketDataClient, CbStockMappingRepo cbStockMappingRepo){
        letProfitRunState = new LetProfitRunState(this, marketDataClient, cbStockMappingRepo);
        normalState = new NormalState(this);
        stopLossState = new StopLossState(this);
        lockProfitState = new LockProfitState(this);
        letProfitRunStockLimitUpState = new LetProfitRunStockLimitUpState(this, marketDataClient, cbStockMappingRepo);

        state = normalState;
    }

    public void updateState(BidAskBrokerDTO bidAskBrokerDTO){
        state.updateState(bidAskBrokerDTO);
    }

    public boolean isSellable(){
        return state.isSellable();
    }

    void setState(State state){
        this.state = state;
    }

    @Override
    public String toString(){
        return "Strategy{" +
                "state='" + state + '\'' +
                ", isSellable=" + this.isSellable() +
                '}';
    }
}
