package com.portfolio.manager.service.sell;

import com.portfolio.manager.constant.Constant;
import com.portfolio.manager.dto.BidAskBrokerDTO;
import com.portfolio.manager.task.TradeTask;
import com.portfolio.manager.util.Util;

import java.time.LocalDateTime;
import java.time.LocalTime;

public class NormalState extends State {
    CrownSellStrategy crownSellStrategy;

    public NormalState(CrownSellStrategy crownSellStrategy) {
        this.crownSellStrategy = crownSellStrategy;
    }

    @Override
    public void updateState(BidAskBrokerDTO bidAskBrokerDTO) {
        if (Util.priceMovementDivide(bidAskBrokerDTO.bidPrice1(), bidAskBrokerDTO.lastClose()).compareTo(Constant.CROWN_TAKE_PROFIT) >= 0 ||
                Util.priceMovementDivide(bidAskBrokerDTO.high(), bidAskBrokerDTO.lastClose()).compareTo(Constant.CROWN_TAKE_PROFIT) >= 0) {
            crownSellStrategy.setState(crownSellStrategy.letProfitRunState);
            return;
        }

        if (this.isLockProfitTime()) {
            if (Util.priceMovementDivide(bidAskBrokerDTO.bidPrice1(), bidAskBrokerDTO.lastClose()).compareTo(Constant.CROWN_LOCK_PROFIT) >= 0 ||
                    Util.priceMovementDivide(bidAskBrokerDTO.high(), bidAskBrokerDTO.lastClose()).compareTo(Constant.CROWN_LOCK_PROFIT) >= 0) {
                crownSellStrategy.setState(crownSellStrategy.lockProfitState);
                return;
            }
        }

        if (Util.priceMovementDivide(bidAskBrokerDTO.askPrice1(), bidAskBrokerDTO.lastClose()).compareTo(TradeTask.getStopLossBar()) <= 0) {
            crownSellStrategy.setState(crownSellStrategy.stopLossState);
        }
    }

    @Override
    public boolean isSellable() {
        return false;
    }

    private boolean isLockProfitTime() {
        LocalTime now = LocalDateTime.now().toLocalTime();
        return now.isAfter(LocalTime.of(10, 30, 30));
    }

}
