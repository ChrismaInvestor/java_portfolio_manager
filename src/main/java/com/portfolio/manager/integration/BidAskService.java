package com.portfolio.manager.integration;

import com.portfolio.manager.dto.BidAskDTO;

public interface BidAskService {
    BidAskDTO getSell1(String securityCode);

    BidAskDTO getBuy1(String securityCode);
}
