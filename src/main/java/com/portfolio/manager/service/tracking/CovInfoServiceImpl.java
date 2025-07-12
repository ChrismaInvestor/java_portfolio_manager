package com.portfolio.manager.service.tracking;

import com.portfolio.manager.domain.CovInfo;
import com.portfolio.manager.dto.integration.BidAskBrokerDTO;
import com.portfolio.manager.integration.MarketDataClient;
import com.portfolio.manager.repository.CovInfoRepo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CovInfoServiceImpl implements CovInfoService{
    @Resource
    CovInfoRepo covInfoRepo;

    @Resource
    MarketDataClient marketDataClient;

    @Override
    public void saveAll(List<CovInfo> covInfoList) {
        Map<String, CovInfo> map = covInfoList.stream().collect(Collectors.toMap(CovInfo::getCode, Function.identity()));
        var oldRecords = covInfoRepo.findAll();
        Set<String> oldCodes = new HashSet<>();
//        Update old records
        oldRecords.forEach(covInfo -> {
            if (map.containsKey(covInfo.getCode())){
                covInfo.setConvertedPrice(covInfo.getConvertedPrice());
            }
            oldCodes.add(covInfo.getCode());
        });
        covInfoRepo.saveAll(oldRecords);
        // Add new records
        map.keySet().removeAll(oldCodes);
        log.info("updated records count: {}", map.keySet().size());
        var newRecords = covInfoList.stream().filter(covInfo -> map.containsKey(covInfo.getCode())).toList();
        covInfoRepo.saveAll(newRecords);
    }

    //溢价率低于30
    @Override
    public List<CovInfo> tradeableCovs() {
        var covList = covInfoRepo.findAll();
        var codePremiumMap = calPremium(covList);
        var filteredCovList = covList.stream().filter(cov -> codePremiumMap.containsKey(cov.getCode()) && codePremiumMap.get(cov.getCode()).compareTo(BigDecimal.valueOf(30L)) <= 0).toList();
        log.info("cov size: {}", filteredCovList.size());
        return filteredCovList;
    }

    @Override
    public Map<String, BigDecimal> calPremium(List<CovInfo> covInfoList){
        var stockCodes = covInfoList.stream().map(CovInfo::getStockCode).toList();
        var covCodes = covInfoList.stream().map(CovInfo::getCode).toList();
        Map<String, Double> stockCodePriceMap = marketDataClient.getBidAsk(stockCodes).stream().collect(Collectors.toMap(BidAskBrokerDTO::securityCode, BidAskBrokerDTO::lastPrice));
        Map<String, Double> covCodePriceMap = marketDataClient.getBidAsk(covCodes).stream().collect(Collectors.toMap(BidAskBrokerDTO::securityCode, BidAskBrokerDTO::lastPrice));
        Map<String, BigDecimal> ans = new HashMap<>();
        covInfoList.forEach(cov -> {
            if (!covCodePriceMap.containsKey(cov.getCode()) || !stockCodePriceMap.containsKey(cov.getStockCode())){
                return;
            }
            double covPrice = covCodePriceMap.get(cov.getCode());
            double stockPrice = stockCodePriceMap.get(cov.getStockCode());
            var actualPremium = this.calPremium(BigDecimal.valueOf(covPrice), BigDecimal.valueOf(stockPrice), BigDecimal.valueOf(cov.getConvertedPrice()));
            ans.put(cov.getCode(), actualPremium);
        });
        return ans;
    }


    static public BigDecimal calPremium(BigDecimal covPrice, BigDecimal stockPrice, BigDecimal convertedPrice){
        var fairPrice = BigDecimal.valueOf(100).multiply(stockPrice).divide(convertedPrice, 2, RoundingMode.HALF_UP);
        return covPrice.divide(fairPrice, 4, RoundingMode.HALF_UP).subtract(BigDecimal.ONE).multiply(BigDecimal.valueOf(100));
    }
}
