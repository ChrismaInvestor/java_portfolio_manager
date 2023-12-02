package com.portfolio.manager.integration;

import com.portfolio.manager.dto.BidAskBrokerDTO;
import com.portfolio.manager.dto.BidAskDTO;

import java.util.List;
import java.util.Map;

public interface BidAskService {

    @Deprecated
    BidAskDTO getSell1(String securityCode);

    @Deprecated
    BidAskDTO getBuy1(String securityCode);

    List<BidAskBrokerDTO> getBidAsk(List<String> securityCodes);
}
