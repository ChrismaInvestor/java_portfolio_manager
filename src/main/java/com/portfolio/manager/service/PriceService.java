package com.portfolio.manager.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface PriceService {
    Double getLatestPrice(String code) throws IOException;

    Map<String, BigDecimal> getVWAPCodePriceMap(List<String> codes);
}
