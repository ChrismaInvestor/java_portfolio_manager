package com.portfolio.manager.strategy;

import com.portfolio.manager.domain.CovInfo;
import com.portfolio.manager.dto.integration.BidAskBrokerDTO;
import com.portfolio.manager.integration.MarketDataClient;
import com.portfolio.manager.service.tracking.CovInfoService;
import com.portfolio.manager.service.tracking.CovInfoServiceImpl;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class CovShakingStrategy {
    /*
      试运行
      买入策略: 正股封板， 实时溢价率低于20%， 封板时点价格或者低于时点价格买入
      卖出策略: 转债价格大于1.01 * 封板时点价格
      撤单策略: 正股破板, 或者可转债最高涨幅超过10%
      止损策略: 正股破板
    */

    //今日可投资可转债清单
    private final List<CovInfo> todayTradeableCovList;

    private final Set<String> blockedList;

    private final MarketDataClient marketDataClient;

    private boolean isBuyCompletion = false;

    private boolean isSellStarted = false;

    private boolean isSellCompletion = false;

    private BigDecimal cash;

    private TargetCov targetCov = null;

    public TargetCov getTargetCov() {
        return targetCov;
    }

    public boolean getIsBuyCompletion() {
        return isBuyCompletion;
    }

    public boolean getIsSellCompletion(){
        return isSellCompletion;
    }

    public CovShakingStrategy(CovInfoService covInfoService, MarketDataClient marketDataClient, Long cash, Set<String> blockedList) {
        todayTradeableCovList = covInfoService.tradeableCovs();
        this.marketDataClient = marketDataClient;
        this.cash = BigDecimal.valueOf(cash);
        this.blockedList = new HashSet<>(blockedList);
    }

    boolean isStockCeiling(BidAskBrokerDTO bidAsk) {
        return bidAsk.askVol1().equals(0) && bidAsk.bidVol1().compareTo(10000) > 0;
    }

    boolean isBelow10(BidAskBrokerDTO covBidAsk) {
        return BigDecimal.valueOf(covBidAsk.high()).divide(BigDecimal.valueOf(covBidAsk.lastClose()), 4, RoundingMode.HALF_UP).compareTo(BigDecimal.valueOf(1.1d)) < 0;
    }

    boolean isPremiumBelow20(double covPrice, double stockPrice, double convertedPrice) {
        BigDecimal actualPremium = CovInfoServiceImpl.calPremium(BigDecimal.valueOf(covPrice), BigDecimal.valueOf(stockPrice), BigDecimal.valueOf(convertedPrice));
        return actualPremium.compareTo(BigDecimal.valueOf(20L)) <= 0;
    }

    public void buy() {
        var stockCodes = todayTradeableCovList.stream().map(CovInfo::getStockCode).collect(Collectors.toSet());
        var covCodes = todayTradeableCovList.stream().map(CovInfo::getCode).toList();
//        检查正股是否涨停, 可转债涨幅是否小于10%， 溢价率是否低于20
        var bidAskMap = marketDataClient.getBidAsk(Stream.concat(stockCodes.stream(), covCodes.stream()).toList()).stream().collect(Collectors.toMap(BidAskBrokerDTO::securityCode, Function.identity()));
        var targetList = todayTradeableCovList.stream().filter(cov -> {
//            market data不包含 或者 属于blocked list
            if (!bidAskMap.containsKey(cov.getStockCode()) || !bidAskMap.containsKey(cov.getCode()) || blockedList.contains(cov.getCode())) {
                return false;
            }
            var stockBidAsk = bidAskMap.get(cov.getStockCode());
            var covBidAsk = bidAskMap.get(cov.getCode());
            if (isStockCeiling(stockBidAsk) && (!isBelow10(covBidAsk) || !isPremiumBelow20(covBidAsk.askPrice1(), stockBidAsk.lastPrice(), cov.getConvertedPrice()))) {
                blockedList.add(covBidAsk.securityCode());
                log.info("blocked list: {}", blockedList);
            }
            return isStockCeiling(stockBidAsk) && isBelow10(covBidAsk) && isPremiumBelow20(covBidAsk.askPrice1(), stockBidAsk.lastPrice(), cov.getConvertedPrice());
        }).toList();
        if (targetList.isEmpty()) {
            return;
        }
        log.info("filtered: {}", targetList);
        targetList.forEach(cov -> targetCov = new TargetCov(cov.getCode(), bidAskMap.get(cov.getCode()).askPrice1(), cov.getStockCode()));
    }

    public void mockOrderPlacementBuy() {
        if (targetCov == null || isBuyCompletion || isSellStarted) {
            return;
        }
        Optional<BidAskBrokerDTO> targetBidAsk = marketDataClient.getBidAsk(List.of(targetCov.getCovCode())).stream().findFirst();
        targetBidAsk.ifPresent(bidAskBrokerDTO -> {
            if (targetCov.getCeilingPrice().compareTo(bidAskBrokerDTO.askPrice1()) >= 0) {
                BigDecimal buyPrice = BigDecimal.valueOf(bidAskBrokerDTO.askPrice1());
                BigDecimal expectedBuyVol = BigDecimal.valueOf(bidAskBrokerDTO.askVol1());
                BigDecimal expectedAmount = buyPrice.multiply(expectedBuyVol);
                BigDecimal actualBuyVol = cash.compareTo(expectedAmount) < 0 ? cash.divide(BigDecimal.TEN, 0, RoundingMode.DOWN).divide(buyPrice, 0, RoundingMode.DOWN).multiply(BigDecimal.TEN) : expectedBuyVol;
                BigDecimal amount = actualBuyVol.multiply(buyPrice);
                if (amount.compareTo(BigDecimal.ZERO) > 0) {
                    log.info("execute buy orders, code: {}, buy price: {}, ceiling price: {}, vol: {}", bidAskBrokerDTO.securityCode(), bidAskBrokerDTO.askPrice1(), targetCov.getCeilingPrice(), actualBuyVol);
                    targetCov.setHoldingVol(targetCov.getHoldingVol() + actualBuyVol.longValue());
                    log.info("target cov: {}", targetCov);
                    cash = cash.subtract(amount);
                } else {
//                    只触发一次
                    isBuyCompletion = true;
                }
//                orderPlacementClient.buy(bidAskBrokerDTO.securityCode(), bidAskBrokerDTO.askPrice1(), bidAskBrokerDTO.askVol1());
            }
        });
    }

    public void sell() {
        if (targetCov == null || targetCov.getHoldingVol().compareTo(0L) <= 0) {
            return;
        }
        Optional<BidAskBrokerDTO> targetBidAsk = marketDataClient.getBidAsk(List.of(targetCov.getCovCode())).stream().findFirst();
        targetBidAsk.ifPresent(bidAskBrokerDTO -> {
            BigDecimal upRatio = BigDecimal.valueOf(bidAskBrokerDTO.bidPrice1()).divide(BigDecimal.valueOf(bidAskBrokerDTO.lastClose()), 4, RoundingMode.HALF_DOWN);
            BigDecimal buyRatio = BigDecimal.valueOf(targetCov.getCeilingPrice()).divide(BigDecimal.valueOf(bidAskBrokerDTO.lastClose()), 4, RoundingMode.HALF_DOWN);
            if (upRatio.subtract(buyRatio).multiply(BigDecimal.valueOf(100L)).compareTo(BigDecimal.ONE) >= 0) {
                isSellStarted = true;
                long vol = targetCov.getHoldingVol() > bidAskBrokerDTO.bidVol1() ? bidAskBrokerDTO.bidVol1() : targetCov.getHoldingVol();
                log.info("execute sell orders, code: {}, sell price: {}, vol: {}", bidAskBrokerDTO.securityCode(), bidAskBrokerDTO.bidPrice1(), vol);
                log.info("target cov: {}", targetCov);
                targetCov.setHoldingVol(targetCov.getHoldingVol() - vol);
                if (targetCov.getHoldingVol().compareTo(0L) <= 0){
                    isSellCompletion = true;
                }
            }
        });
    }

    void cancel() {

    }

}
