package com.portfolio.manager.service.sell;

import com.portfolio.manager.service.PriceService;
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

    VWAP(PriceService priceService){
        this.priceService = priceService;
        codes = new HashSet<>();
        codeVWAPMap = new HashMap<>();
    }

    public void addCode(String code){
        codes.add(code);
    }

    public boolean containsCode(String code){
        return codes.contains(code);
    }

    public void update(){
        if (codes.isEmpty()){
            return;
        }
        log.info("vwap codes: {}", codes);
        codeVWAPMap = priceService.getVWAPCodePriceMap(codes.stream().toList());
    }

    public BigDecimal getPrice(String code){
        if (codeVWAPMap.containsKey(code)){
            return  codeVWAPMap.get(code);
        }
        return null;
    }
}
