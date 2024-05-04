package com.portfolio.manager.service.sell;

import com.portfolio.manager.constant.Constant;
import com.portfolio.manager.domain.strategy_specific.CbStockMapping;
import com.portfolio.manager.dto.BidAskBrokerDTO;
import com.portfolio.manager.integration.MarketDataClient;
import com.portfolio.manager.repository.CbStockMappingRepo;
import com.portfolio.manager.util.Util;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class LetProfitRunState extends State {
    CrownSellStrategy crownSellStrategy;

    MarketDataClient marketDataClient;

    CbStockMappingRepo cbStockMappingRepo;

    public LetProfitRunState(CrownSellStrategy crownSellStrategy, MarketDataClient marketDataClient, CbStockMappingRepo cbStockMappingRepo) {
        this.crownSellStrategy = crownSellStrategy;
        this.marketDataClient = marketDataClient;
        this.cbStockMappingRepo = cbStockMappingRepo;
    }

    @Override
    public void updateState(BidAskBrokerDTO bidAskBrokerDTO) {
        log.info("code: {}, ask price: {}, ask price2: {}", bidAskBrokerDTO.securityCode(), bidAskBrokerDTO.askPrice1(), bidAskBrokerDTO.askPrice2());
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
    }

    @Override
    public boolean isSellable() {
        return false;
    }
}
