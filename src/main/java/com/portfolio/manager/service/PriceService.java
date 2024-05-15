package com.portfolio.manager.service;

import com.portfolio.manager.domain.Price;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface PriceService {
    Double getLatestPrice(String code) throws IOException;

    Map<String, List<Price>> getVWAPCodePriceMap(List<String> codes);
}
