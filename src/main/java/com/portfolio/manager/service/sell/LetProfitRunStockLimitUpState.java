package com.portfolio.manager.service.sell;

import com.portfolio.manager.constant.Constant;
import com.portfolio.manager.domain.strategy_specific.CbStockMapping;
import com.portfolio.manager.dto.integration.BidAskBrokerDTO;
import com.portfolio.manager.integration.MarketDataClient;
import com.portfolio.manager.repository.CbStockMappingRepo;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Slf4j
public class LetProfitRunStockLimitUpState extends State{
    CrownSellStrategy crownSellStrategy;

    MarketDataClient marketDataClient;

    CbStockMappingRepo cbStockMappingRepo;

    VWAP vwap;

    public LetProfitRunStockLimitUpState(CrownSellStrategy crownSellStrategy, MarketDataClient marketDataClient, CbStockMappingRepo cbStockMappingRepo, VWAP vwap){
        this.crownSellStrategy = crownSellStrategy;
        this.marketDataClient = marketDataClient;
        this.cbStockMappingRepo = cbStockMappingRepo;
        this.vwap = vwap;
    }
    @Override
    public void updateState(BidAskBrokerDTO bidAskBrokerDTO) {
        super.updateBid1PricesSlidingWindow(bidAskBrokerDTO);
        if (!vwap.containsCode(bidAskBrokerDTO.securityCode())) {
            vwap.addCode(bidAskBrokerDTO.securityCode());
        }
        Optional<CbStockMapping> mapping = cbStockMappingRepo.findByCbCode(bidAskBrokerDTO.securityCode());
        mapping.ifPresent(v -> {
            Optional<BidAskBrokerDTO> bidAsk = marketDataClient.getBidAsk(List.of(v.getStockCode())).stream().findFirst();
            bidAsk.ifPresent(bidAskBroker -> {
                if (bidAskBroker.askVol2() > 0) {
                    crownSellStrategy.setState(crownSellStrategy.stopLossState);
                }
            });
        });

        log.info("code: {}, vwap price: {}", bidAskBrokerDTO.securityCode(), vwap.getPrice(bidAskBrokerDTO.securityCode()));
        if (vwap.getPrice(bidAskBrokerDTO.securityCode()) != null && BigDecimal.valueOf(bidAskBrokerDTO.askPrice1()).compareTo(vwap.getPrice(bidAskBrokerDTO.securityCode()).subtract(Constant.CROWN_VWAP_BUFFER.multiply(BigDecimal.valueOf(bidAskBrokerDTO.lastClose())))) <= 0) {
            crownSellStrategy.setState(crownSellStrategy.stopLossState);
        }
    }

    @Override
    public boolean isSellable() {
        return false;
    }

}
