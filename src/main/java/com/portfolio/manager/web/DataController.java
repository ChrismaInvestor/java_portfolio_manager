package com.portfolio.manager.web;

import com.portfolio.manager.domain.Security;
import com.portfolio.manager.integration.MarketDataService;
import com.portfolio.manager.service.PriceService;
import com.portfolio.manager.service.SecurityService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@RequestMapping("data")
@RestController
@CrossOrigin
public class DataController {
    @Resource
    MarketDataService marketDataService;

    @Resource
    SecurityService securityService;

    @Resource
    PriceService priceService;

    @GetMapping("stockList")
    public Map<String, String> syncStockList() {
        Map<String, Security> map = securityService.listExistingStocks().stream().collect(Collectors.toMap(Security::getCode, Function.identity()));
        AtomicInteger count = new AtomicInteger(0);
        marketDataService.listAllStocksInfo().forEach(v -> {
            if (!map.containsKey(v.code())) {
                Security security = new Security();
                security.setCode(v.code());
                security.setName(v.name());
                count.incrementAndGet();
                log.info("security: {}", security);
                securityService.addSecurity(security);
            }
        });
        return Map.of("data", String.format("共新增%s条股票记录, 更新时间%s", count.get(), LocalDateTime.now()));
    }

    @GetMapping("stockUpdateInfo")
    public String getStockUpdateInfo() {
        return null;
    }

    @GetMapping("minPrice")
    public void syncMinPrice() {
        //删除过期数据
        priceService.deletePricesMoreThan30Days();
        //新增新数据
//        securityService.listExistingStocks()
//        return Map.of("data", String.format("共新增%s条分时记录, 共删除%s条分时记录 更新时间%s", count.get(), LocalDateTime.now()));
    }
}
