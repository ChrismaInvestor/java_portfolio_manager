package com.portfolio.manager.service;

import com.portfolio.manager.domain.Price;
import com.portfolio.manager.repository.PriceRepo;
import com.portfolio.manager.repository.SecurityRepo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PriceServiceImpl implements PriceService {
    @Resource
    PriceRepo priceRepo;

    @Resource
    SecurityRepo securityRepo;

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

    @Override
    public void checkIntegrityOfMinutePrices() {
        securityRepo.findAll().forEach(security -> {
            Map<LocalDate, List<Price>> prices = priceRepo.findAllByCode(security.getCode()).stream().collect(Collectors.groupingBy(
                    price -> price.getTime().toLocalDate()
            ));
            prices.forEach((key, value)->{
                if (value.size()< 237){
                    log.error("date: {}, security: {}", key, security.getCode());
                }
            });
        });
    }
}
