package com.portfolio.manager.service.new_sell;

import com.portfolio.manager.dto.integration.BidAskBrokerDTO;
import com.portfolio.manager.util.Util;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Slf4j
public class NormalState extends State {
    public NormalState(BigDecimal downThreshold, BigDecimal upThreshold) {
        super(downThreshold, upThreshold);
    }

    @Override
    public boolean isSellable(BidAskBrokerDTO bidAskBrokerDTO) {
        if (accessTime == null) {
            accessTime = LocalDateTime.now();
        }
        long minutesDifference = ChronoUnit.MINUTES.between(accessTime, LocalDateTime.now());
        if (minutesDifference < 15) {
            log.info("difference: {}", minutesDifference);
            return Util.priceMovementDivide(bidAskBrokerDTO.askPrice1(), bidAskBrokerDTO.lastClose()).compareTo(downThreshold.add(new BigDecimal("-0.005"))) < 0;
        }
        return Util.priceMovementDivide(bidAskBrokerDTO.askPrice1(), bidAskBrokerDTO.lastClose()).compareTo(downThreshold) < 0;
    }
}
