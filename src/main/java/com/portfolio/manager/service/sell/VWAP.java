package com.portfolio.manager.service.sell;

import com.portfolio.manager.domain.Price;
import com.portfolio.manager.service.PriceService;
import com.portfolio.manager.util.Util;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;

@Slf4j
@Component
public class VWAP {
    @Resource
    PriceService priceService;

    Set<String> codes;

    Map<String, BigDecimal> codeVWAPMap;

    Map<String, BigDecimal> codeMaxMinuteVWAPPrice;

    VWAP(PriceService priceService) {
        this.priceService = priceService;
        codes = new HashSet<>();
        codeVWAPMap = new HashMap<>();
        codeMaxMinuteVWAPPrice = new HashMap<>();
    }

    public void addCode(String code) {
        codes.add(code);
    }

    public boolean containsCode(String code) {
        return codes.contains(code);
    }

    public void update() {
        if (codes.isEmpty()) {
            return;
        }
        log.info("vwap codes: {}", codes);
        Map<String, List<Price>> map = priceService.getVWAPCodePriceMap(codes.stream().toList());
        Map<String, BigDecimal> codeVWAPMap = new HashMap<>();
        Map<String, BigDecimal> codeMaxMinuteVWAPPrice = new HashMap<>();
        map.forEach((k, v) -> {
            BigDecimal maxMinuteVWAPPrice = BigDecimal.ZERO;
            for (Price price : v) {
                if (price.getVolume() > 0) {
                    BigDecimal minuteVWAPPrice = Util.priceMovementDivide(price.getAmount(), (double) price.getVolume());
                    if (minuteVWAPPrice.compareTo(maxMinuteVWAPPrice) > 0) {
                        maxMinuteVWAPPrice = minuteVWAPPrice;
                    }
                }
            }
            codeMaxMinuteVWAPPrice.put(k, maxMinuteVWAPPrice);
            long volume = v.stream().mapToLong(Price::getVolume).sum();
            Double amount = v.stream().mapToDouble(Price::getAmount).sum();
            codeVWAPMap.put(k, Util.priceMovementDivide(amount, (double) volume));
        });
        this.codeVWAPMap = codeVWAPMap;
        this.codeMaxMinuteVWAPPrice = codeMaxMinuteVWAPPrice;
        log.info("code max minute vwap price: {}", codeMaxMinuteVWAPPrice);
    }

    public BigDecimal getPrice(String code) {
        if (codeVWAPMap.containsKey(code)) {
            return codeVWAPMap.get(code);
        }
        return null;
    }

    public BigDecimal getMaxMinuteVWAPPrice(String code) {
        if (codeMaxMinuteVWAPPrice.containsKey(code)) {
            return codeMaxMinuteVWAPPrice.get(code);
        }
        return null;
    }
}
