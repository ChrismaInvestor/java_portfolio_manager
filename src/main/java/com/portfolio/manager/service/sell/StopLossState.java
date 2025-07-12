package com.portfolio.manager.service.sell;

import com.portfolio.manager.dto.integration.BidAskBrokerDTO;

public class StopLossState extends State{
    CrownSellStrategy crownSellStrategy;

    public StopLossState(CrownSellStrategy crownSellStrategy){
        this.crownSellStrategy = crownSellStrategy;
    }
    @Override
    public void updateState(BidAskBrokerDTO bidAskBrokerDTO) {

    }

    @Override
    public boolean isSellable() {
        return true;
    }
}
