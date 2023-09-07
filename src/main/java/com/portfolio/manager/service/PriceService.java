package com.portfolio.manager.service;

import com.portfolio.manager.domain.Price;

import java.util.List;

public interface PriceService {
    Price getLatestPrice(String code);

    void addPrice(List<Price> prices);
}
