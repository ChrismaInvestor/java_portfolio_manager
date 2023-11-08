package com.portfolio.manager.service;

import com.portfolio.manager.domain.Price;
import com.portfolio.manager.repository.PriceRepo;
import com.portfolio.manager.repository.SecurityRepo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
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
        return priceRepo.findTopByCodeOrderByTimeDesc(code);
    }

    @Override
    public void addPrice(List<Price> prices) {
        prices.stream().parallel().forEach(price -> {
            BigDecimal bdPrice = BigDecimal.valueOf(price.getPrice());
            bdPrice = bdPrice.setScale(2, RoundingMode.FLOOR);
            price.setPrice(bdPrice.doubleValue());
        });
        Map<LocalDate, List<Price>> pricesMap = prices.stream().parallel().collect(Collectors.groupingBy(
                price -> price.getTime().toLocalDate()
        ));
        pricesMap.values().stream().parallel().forEach(pricesToSave -> {
            try {
                priceRepo.saveAll(pricesToSave);
            } catch (Exception e) {
                log.warn("same record");
            }
        });

    }

    @Override
    public void checkIntegrityOfMinutePrices() {
        securityRepo.findAll().stream().parallel().forEach(security -> {
            Map<LocalDate, List<Price>> prices = priceRepo.findAllByCode(security.getCode()).stream().collect(Collectors.groupingBy(
                    price -> price.getTime().toLocalDate()
            ));
            prices.forEach((key, value) -> {
                LocalTime time = value.get(0).getTime().toLocalTime();
                if (time.isAfter(LocalTime.of(10, 0, 0))) {
                    log.error("date: {}, security: {}, start time: {}", key, security.getCode(), time);
                    priceRepo.deleteAll(value);
                }
            });
        });
    }

    @Override
    public long deletePricesMoreThan30Days() {
        final LocalDateTime now = LocalDateTime.now();
        AtomicInteger count = new AtomicInteger(0);
        securityRepo.findAll().stream().parallel().forEach(security -> {
            Map<LocalDate, List<Price>> prices = priceRepo.findAllByCode(security.getCode()).stream().parallel().collect(Collectors.groupingBy(
                    price -> price.getTime().toLocalDate()
            ));
            prices.entrySet().stream().parallel().forEach(entry -> {
                if (Duration.between(LocalDateTime.of(entry.getKey(), LocalTime.now()), now).toDays() > 30) {
                    priceRepo.deleteAllInBatch(entry.getValue());
                    count.addAndGet(entry.getValue().size());
                }
            });
        });
        return count.get();
    }
}
