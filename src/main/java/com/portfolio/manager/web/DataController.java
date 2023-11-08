package com.portfolio.manager.web;

import com.portfolio.manager.domain.Price;
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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.spi.CurrencyNameProvider;
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
        marketDataService.listAllStocksInfo().stream().parallel().forEach(v -> {
            if (!map.containsKey(v.code())) {
                Security security = new Security();
                security.setCode(v.code());
                security.setName(v.name());
                count.incrementAndGet();
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
    public Map<String, String> syncMinPrice() {
        //删除过期数据
        long deletionCount = priceService.deletePricesMoreThan30Days();

        AtomicInteger addCount = new AtomicInteger(0);

//        ForkJoinPool customThreadPool = new ForkJoinPool(2);
        //新增新数据
//        customThreadPool.submit(() -> securityService.listExistingStocks().parallelStream().forEach(s -> {
        ConcurrentLinkedDeque<String> codesDeque = new ConcurrentLinkedDeque<>();
        securityService.listExistingStocks().stream().parallel().forEach(s -> {
                    codesDeque.addLast(s.getCode());
//                    List<Price> minPrices = marketDataService.listMinPrice(s.getCode());
//                    priceService.addPrice(minPrices);
//                    addCount.addAndGet(minPrices.size());
                }
        );
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(4);
        executor.scheduleAtFixedRate(()->{
//            while(!codesDeque.isEmpty()){
                List<Price> minPrices = marketDataService.listMinPrice(codesDeque.pollFirst());
                priceService.addPrice(minPrices);
                addCount.addAndGet(minPrices.size());
//            }
        },0,1, TimeUnit.SECONDS);

//        executor.shutdown();
        return Map.of("data", String.format("当前分时记录起始日期%s, 结束日期%s条 更新时间%s", deletionCount, addCount.get(), LocalDateTime.now()));
    }
}
