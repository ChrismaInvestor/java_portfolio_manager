package com.portfolio.manager.service;

import com.portfolio.manager.domain.Price;
import com.portfolio.manager.repository.PriceRepo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class PriceServiceImpl implements PriceService{
    @Resource
    PriceRepo priceRepo;

    @Override
    public Price getLatestPrice(String code) {
        return null;
    }

    @Override
    public void addPrice(Price price) {
        priceRepo.save(price);
    }
}
