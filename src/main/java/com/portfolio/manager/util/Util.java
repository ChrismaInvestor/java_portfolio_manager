package com.portfolio.manager.util;

import com.portfolio.manager.domain.Investor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class Util {

    public static Map<String, BigDecimal> getPortfolioSharesMap(List<Investor> investors){
        Map<String, BigDecimal> portfolioSharesMap = new HashMap<>();

        investors.forEach(investor -> {
            if (portfolioSharesMap.containsKey(investor.getPortfolioName())){
                var newResult = portfolioSharesMap.get(investor.getPortfolioName()).add(investor.getShareAmount());
                portfolioSharesMap.put(investor.getPortfolioName(), newResult);
            }else{
                portfolioSharesMap.put(investor.getPortfolioName(), investor.getShareAmount());
            }
        });

        return portfolioSharesMap;
    }

    public static Long calVolume(Long volume, Double discount, Long multiple){
        BigDecimal multipleBd = BigDecimal.valueOf(multiple);
        BigDecimal dividedVol = BigDecimal.valueOf(volume).divide(multipleBd, RoundingMode.UNNECESSARY);
        return dividedVol.multiply(BigDecimal.valueOf(discount)).setScale(0, RoundingMode.UP).multiply(multipleBd).longValue();
    }

    public static boolean isTradingDay() {
        LocalDate today = LocalDate.now();
        return !today.getDayOfWeek().equals(DayOfWeek.SUNDAY) && !today.getDayOfWeek().equals(DayOfWeek.SATURDAY);
    }

    public static BigDecimal priceMovementDivide(Double numerator, Double denominator){
        return BigDecimal.valueOf(numerator).divide(BigDecimal.valueOf(denominator), 4, RoundingMode.HALF_EVEN);
    }

    public static void main(String[] args) {
        long ans = Util.calVolume(2190L, 0.5, 10L);
        log.info("{}", ans);
        log.info("is: {}", isTradingDay());
    }
}
