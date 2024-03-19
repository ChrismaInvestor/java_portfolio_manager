package com.portfolio.manager.dto;

public record BidAskBrokerDTO(String securityCode, Double askPrice1, Double bidPrice1, Integer askVol1, Integer bidVol1, Double lastPrice, Double lastClose, Integer askVol2, Double askPrice2, Integer bidVol2, Double bidPrice2, Double high) {
}
