package com.portfolio.manager.service.sell;

import com.portfolio.manager.constant.Constant;
import com.portfolio.manager.dto.BidAskBrokerDTO;
import com.portfolio.manager.util.Util;

import java.math.BigDecimal;

public class LockProfitState extends State {
    CrownSellStrategy crownSellStrategy;
    VWAP vwap;

    public LockProfitState(CrownSellStrategy crownSellStrategy, VWAP vwap) {
        this.crownSellStrategy = crownSellStrategy;
        this.vwap = vwap;
    }

    @Override
    public void updateState(BidAskBrokerDTO bidAskBrokerDTO) {
        super.updateBid1PricesSlidingWindow(bidAskBrokerDTO);
        if (!vwap.containsCode(bidAskBrokerDTO.securityCode())) {
            vwap.addCode(bidAskBrokerDTO.securityCode());
        }

        if (Util.priceMovementDivide(bidAskBrokerDTO.bidPrice1(), bidAskBrokerDTO.lastClose()).compareTo(Constant.CROWN_TAKE_PROFIT) >= 0 ||
                Util.priceMovementDivide(bidAskBrokerDTO.high(), bidAskBrokerDTO.lastClose()).compareTo(Constant.CROWN_TAKE_PROFIT) >= 0) {
            crownSellStrategy.setState(crownSellStrategy.letProfitRunState);
            return;
        }

        if(BigDecimal.valueOf(bidAskBrokerDTO.askPrice1()).compareTo(BigDecimal.valueOf(bidAskBrokerDTO.lastClose())) <= 0) {
            crownSellStrategy.setState(crownSellStrategy.stopLossState);
            return;
        }

        BigDecimal maxMinuteVWAPPrice = vwap.getMaxMinuteVWAPPrice(bidAskBrokerDTO.securityCode());

        if (maxMinuteVWAPPrice !=null && Util.priceMovementDivide(maxMinuteVWAPPrice.subtract(BigDecimal.valueOf(bidAskBrokerDTO.askPrice1())).doubleValue(), bidAskBrokerDTO.lastClose()).compareTo(Constant.CROWN_MAX_DRAW_DOWN) >= 0) {
            crownSellStrategy.setState(crownSellStrategy.stopLossState);
        }
    }

    @Override
    public boolean isSellable() {
        return false;
    }
}
