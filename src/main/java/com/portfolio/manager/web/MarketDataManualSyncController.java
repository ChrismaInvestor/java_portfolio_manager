package com.portfolio.manager.web;

import com.portfolio.manager.domain.Security;
import com.portfolio.manager.integration.MarketDataClient;
import com.portfolio.manager.repository.CbStockMappingRepo;
import com.portfolio.manager.service.SecurityService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@RequestMapping("data")
@RestController
@CrossOrigin
public class MarketDataManualSyncController {
    @Resource
    MarketDataClient marketDataClient;

    @Resource
    SecurityService securityService;

    @Resource
    CbStockMappingRepo cbStockMappingRepo;

    @GetMapping("stockList")
    public Map<String, String> syncStockList() {
        AtomicInteger count = new AtomicInteger(0);
        securityService.clearData();
        marketDataClient.listAllStocksInfo().forEach(v -> {
                Security security = new Security();
                security.setCode(v.code());
                security.setName(v.name());
                count.incrementAndGet();
                securityService.addSecurity(security);
        });

        Set<String> codes = securityService.listExistingStocks().stream().map(Security::getCode).collect(Collectors.toSet());
        marketDataClient.listCbStockMapping().forEach(v ->{
            if (codes.contains(v.getCbCode())){
                log.info("code: {}", v);
                cbStockMappingRepo.save(v);
            }
        });
        return Map.of("data", String.format("共新增%s条股票记录, 更新时间%s", count.get(), LocalDateTime.now()));
    }

}
