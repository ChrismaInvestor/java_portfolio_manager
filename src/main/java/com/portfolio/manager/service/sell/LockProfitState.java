package com.portfolio.manager.service.sell;

import com.portfolio.manager.constant.Constant;
import com.portfolio.manager.dto.BidAskBrokerDTO;
import com.portfolio.manager.util.Util;

import java.math.BigDecimal;

public class LockProfitState extends State {
    CrownSellStrategy crownSellStrategy;

    public LockProfitState(CrownSellStrategy crownSellStrategy) {
        this.crownSellStrategy = crownSellStrategy;
    }

    @Override
    public void updateState(BidAskBrokerDTO bidAskBrokerDTO) {
        super.updateBid1PricesSlidingWindow(bidAskBrokerDTO);

        if (Util.priceMovementDivide(bidAskBrokerDTO.bidPrice1(), bidAskBrokerDTO.lastClose()).compareTo(Constant.CROWN_TAKE_PROFIT) >= 0 ||
                Util.priceMovementDivide(bidAskBrokerDTO.high(), bidAskBrokerDTO.lastClose()).compareTo(Constant.CROWN_TAKE_PROFIT) >= 0) {
            crownSellStrategy.setState(crownSellStrategy.letProfitRunState);
            return;
        }

        if (Util.priceMovementDivide(BigDecimal.valueOf(bidAskBrokerDTO.high()).subtract(BigDecimal.valueOf(bidAskBrokerDTO.askPrice1())).doubleValue(), bidAskBrokerDTO.lastClose()).compareTo(Constant.CROWN_MAX_DRAW_DOWN) >= 0 || BigDecimal.valueOf(bidAskBrokerDTO.askPrice1()).compareTo(BigDecimal.valueOf(bidAskBrokerDTO.lastClose())) <= 0) {
            crownSellStrategy.setState(crownSellStrategy.stopLossState);
        }
    }

    @Override
    public boolean isSellable() {
        return false;
    }
}
