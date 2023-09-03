package com.portfolio.manager.web;

import com.portfolio.manager.domain.Price;
import com.portfolio.manager.service.PriceService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/price")
@RestController
public class PriceController {

    @Resource
    PriceService priceService;

    @PostMapping
    public void addPrice(@RequestBody Price price){
        priceService.addPrice(price);
    }
}
