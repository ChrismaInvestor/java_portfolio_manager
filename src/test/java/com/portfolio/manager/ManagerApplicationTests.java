package com.portfolio.manager;

import com.portfolio.manager.service.PortfolioService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
class ManagerApplicationTests {
    @Resource
    PortfolioService portfolioService;

    @Test
    void contextLoads() {
//        Portfolio portfolio = portfolioService.listPortfolio().get(0);
//        List<Position> positions = portfolioService.listPosition(portfolio.getName());
//        log.info("{}", BigDecimal.valueOf(50L).divide(BigDecimal.valueOf(1330L), 2, RoundingMode.HALF_UP).compareTo(BigDecimal.valueOf(0.2d)) < 0);
//        BidAskBrokerDTO bidAskBrokerDTO = new BidAskBrokerDTO();
//        tradeTaskService.autoSellForCrown(bidAskBrokerDTO,portfolio,positions);
    }

}
