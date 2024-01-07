package com.portfolio.manager.util;

import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Slf4j
public class Util {

    public static Long calVolume(Long volume, Double discount, Long multiple){
        BigDecimal multipleBd = BigDecimal.valueOf(multiple);
        BigDecimal dividedVol = BigDecimal.valueOf(volume).divide(multipleBd, RoundingMode.UNNECESSARY);
        return dividedVol.multiply(BigDecimal.valueOf(discount)).setScale(0, RoundingMode.UP).multiply(multipleBd).longValue();
    }

    public static void main(String[] args) {
        long ans = Util.calVolume(2190L, 0.5, 10L);
        log.info("{}", ans);
    }
}
