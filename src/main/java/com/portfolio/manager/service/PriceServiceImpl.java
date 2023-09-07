package com.portfolio.manager.service;

import com.portfolio.manager.domain.Price;
import com.portfolio.manager.repository.PriceRepo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class PriceServiceImpl implements PriceService{
    @Resource
    PriceRepo priceRepo;

    @Override
    public Price getLatestPrice(String code) {
        return null;
    }

    @Override
    public void addPrice(List<Price> prices) {
        prices.forEach(price -> {
            BigDecimal bdPrice = BigDecimal.valueOf(price.getPrice());
            bdPrice = bdPrice.setScale(2, RoundingMode.FLOOR);
            price.setPrice(bdPrice.doubleValue());
        });
        priceRepo.saveAll(prices);

    }
}
