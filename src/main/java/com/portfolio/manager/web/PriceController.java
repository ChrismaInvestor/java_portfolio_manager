package com.portfolio.manager.web;

import com.portfolio.manager.domain.Price;
import com.portfolio.manager.service.PriceService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RequestMapping("/price")
@RestController
@Slf4j
public class PriceController {

    @Resource
    PriceService priceService;

    @PostMapping
    public void addPrice(@RequestBody List<Price> minPricesOfDays){
        priceService.addPrice(minPricesOfDays);
    }
}
