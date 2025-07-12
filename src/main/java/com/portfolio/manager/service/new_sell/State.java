package com.portfolio.manager.service.new_sell;

import com.portfolio.manager.dto.integration.BidAskBrokerDTO;
import com.portfolio.manager.util.Util;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
public abstract class State {
    final BigDecimal downThreshold;
    final BigDecimal upThreshold;

    LocalDateTime accessTime = null;

    State(BigDecimal downThreshold, BigDecimal upThreshold) {
        this.downThreshold = downThreshold;
        this.upThreshold = upThreshold;
    }

    public boolean isSellable(BidAskBrokerDTO bidAskBrokerDTO) {
        if (accessTime == null) {
            accessTime = LocalDateTime.now();
        }
        log.info("ratio: {}", Util.priceMovementDivide(bidAskBrokerDTO.askPrice1(), bidAskBrokerDTO.lastClose()));
        return Util.priceMovementDivide(bidAskBrokerDTO.askPrice1(), bidAskBrokerDTO.lastClose()).compareTo(downThreshold) < 0;
    }

    public boolean isUpgradable(BidAskBrokerDTO bidAskBrokerDTO) {
        if (upThreshold == null) {
            return false;
        }
        return Util.priceMovementDivide(bidAskBrokerDTO.bidPrice1(), bidAskBrokerDTO.lastClose()).compareTo(upThreshold) > 0;
    }


    @Override
    public String toString() {
        return this.getClass().getName() + "[upThreshold=" + upThreshold + ", downThreshold=" + downThreshold + ",access time=" + accessTime + "]";
    }
}
