package com.portfolio.manager.dto;

import java.util.List;

public record BidAskBrokerDTO(String securityCode, Double askPrice1, Double bidPrice1, Integer askVol1, Integer bidVol1) {
}
