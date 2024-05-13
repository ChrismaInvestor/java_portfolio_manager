package com.portfolio.manager.service.sell;

import com.portfolio.manager.constant.Constant;
import com.portfolio.manager.domain.strategy_specific.CbStockMapping;
import com.portfolio.manager.dto.BidAskBrokerDTO;
import com.portfolio.manager.integration.MarketDataClient;
import com.portfolio.manager.repository.CbStockMappingRepo;
import com.portfolio.manager.task.TradeTask;
import com.portfolio.manager.util.Util;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class LetProfitRunState extends State {
    CrownSellStrategy crownSellStrategy;

    MarketDataClient marketDataClient;

    CbStockMappingRepo cbStockMappingRepo;

    VWAP vwap;

    public LetProfitRunState(CrownSellStrategy crownSellStrategy, MarketDataClient marketDataClient, CbStockMappingRepo cbStockMappingRepo, VWAP vwap) {
        this.crownSellStrategy = crownSellStrategy;
        this.marketDataClient = marketDataClient;
        this.cbStockMappingRepo = cbStockMappingRepo;
        this.vwap = vwap;
    }

    @Override
    public void updateState(BidAskBrokerDTO bidAskBrokerDTO) {
        if (this.isLetProfitRunTime()) {
            if (!vwap.containsCode(bidAskBrokerDTO.securityCode())) {
                vwap.addCode(bidAskBrokerDTO.securityCode());
            }
            AtomicBoolean isStateChanged = new AtomicBoolean(false);
            Optional<CbStockMapping> mapping = cbStockMappingRepo.findByCbCode(bidAskBrokerDTO.securityCode());
            mapping.ifPresent(v -> {
                Optional<BidAskBrokerDTO> bidAsk = marketDataClient.getBidAsk(List.of(v.getStockCode())).stream().findFirst();
                bidAsk.ifPresent(bidAskBroker -> {
                    if (bidAskBroker.askVol1() == 0) {
                        crownSellStrategy.setState(crownSellStrategy.letProfitRunStockLimitUpState);
                        isStateChanged.set(true);
                    }
                });
            });
            if (!isStateChanged.get() && Util.priceMovementDivide(bidAskBrokerDTO.askPrice1(), bidAskBrokerDTO.lastClose()).compareTo(Constant.CROWN_LET_PROFIT_RUN_TAKE_PROFIT) < 0) {
                crownSellStrategy.setState(crownSellStrategy.stopLossState);
            }

            log.info("code: {}, vwap price: {}", bidAskBrokerDTO.securityCode(), vwap.getPrice(bidAskBrokerDTO.securityCode()));
//            观察者模式
            if (vwap.getPrice(bidAskBrokerDTO.securityCode()) != null && BigDecimal.valueOf(bidAskBrokerDTO.askPrice1()).compareTo(vwap.getPrice(bidAskBrokerDTO.securityCode()).subtract(Constant.CROWN_VWAP_BUFFER.multiply(BigDecimal.valueOf(bidAskBrokerDTO.lastClose())))) <= 0) {
                crownSellStrategy.setState(crownSellStrategy.stopLossState);
            }

        } else {
            if (Util.priceMovementDivide(bidAskBrokerDTO.askPrice1(), bidAskBrokerDTO.lastClose()).compareTo(TradeTask.getStopLossBar()) <= 0) {
                crownSellStrategy.setState(crownSellStrategy.stopLossState);
            }
        }


    }

    @Override
    public boolean isSellable() {
        return false;
    }

    private boolean isLetProfitRunTime() {
        LocalTime now = LocalDateTime.now().toLocalTime();
        return now.isAfter(LocalTime.of(10, 0, 30));
    }
}
