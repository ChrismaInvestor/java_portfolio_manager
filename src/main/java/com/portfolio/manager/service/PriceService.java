package com.portfolio.manager.service;

import com.portfolio.manager.domain.Price;

public interface PriceService {
    Price getLatestPrice(String code);

    void addPrice(Price price);
}
