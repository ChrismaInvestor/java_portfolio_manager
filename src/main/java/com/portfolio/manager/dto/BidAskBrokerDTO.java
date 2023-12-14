package com.portfolio.manager.dto;

public record BidAskBrokerDTO(String securityCode, Double askPrice1, Double bidPrice1, Integer askVol1, Integer bidVol1, Double lastPrice, Double lastClose) {
}
