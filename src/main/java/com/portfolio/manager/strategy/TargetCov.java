package com.portfolio.manager.strategy;

import lombok.ToString;

@ToString
public class TargetCov {
    private final String covCode;

    private final String stockCode;

    private final Double ceilingPrice;

    private Long holdingVol;

    public TargetCov(String covCode, Double ceilingPrice, String stockCode){
        this.covCode = covCode;
        this.ceilingPrice = ceilingPrice;
        this.holdingVol = 0L;
        this.stockCode = stockCode;
    }

    public String getCovCode(){
        return covCode;
    }

    public Double getCeilingPrice(){
        return ceilingPrice;
    }

    public String getStockCode(){ return stockCode;}

    public void setHoldingVol(Long holdingVol){
        this.holdingVol = holdingVol;
    }

    public Long getHoldingVol(){
        return holdingVol;
    }

}
