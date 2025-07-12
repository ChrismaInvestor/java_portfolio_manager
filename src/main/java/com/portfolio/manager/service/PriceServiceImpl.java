package com.portfolio.manager.service;

import com.portfolio.manager.domain.Price;
import com.portfolio.manager.integration.MarketDataClient;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PriceServiceImpl implements PriceService {

    @Resource
    MarketDataClient marketDataClient;

    @Override
    public Double getLatestPrice(String code) throws IOException {
        var bidAskList = marketDataClient.getBidAsk(List.of(code));
        if (bidAskList.isEmpty()){
            throw new IOException("Price quote met issues");
        }
        Double askPrice1 = bidAskList.get(0).askPrice1();
        if (BigDecimal.valueOf(askPrice1).compareTo(BigDecimal.ZERO) > 0){
            return askPrice1;
        }
        return bidAskList.get(0).lastPrice();
    }

    @Override
//    public Map<String, BigDecimal> getVWAPCodePriceMap(List<String> codes) {
    public Map<String, List<Price>> getVWAPCodePriceMap(List<String> codes) {
        var minPrices = marketDataClient.listMinPrice(codes);
//        Map<String, List<Price>> map = minPrices.stream().collect(Collectors.groupingBy(Price::getCode));
        return minPrices.stream().collect(Collectors.groupingBy(Price::getCode));
//        Map<String, BigDecimal> ans = new HashMap<>();
//        map.forEach((k,v )->{
//            long volume = v.stream().mapToLong(Price::getVolume).sum();
//            Double amount = v.stream().mapToDouble(Price::getAmount).sum();
//            ans.put(k, Util.priceMovementDivide(amount, (double) volume));
//        });
//        return ans;
    }

}
