package com.portfolio.manager.service.sell;

import com.portfolio.manager.dto.BidAskBrokerDTO;

public abstract class State {

    abstract void updateState(BidAskBrokerDTO bidAskBrokerDTO);

    abstract boolean isSellable();

    public String toString(){
        return this.getClass().getName();
    }
}
