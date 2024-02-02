package com.portfolio.manager;

import com.portfolio.manager.domain.Portfolio;
import com.portfolio.manager.domain.Position;
import com.portfolio.manager.dto.BidAskBrokerDTO;
import com.portfolio.manager.service.PortfolioService;
import com.portfolio.manager.task.TradeTaskService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class ManagerApplicationTests {
    @Resource
    PortfolioService portfolioService;

    @Resource
    TradeTaskService tradeTaskService;

    @Test
    void contextLoads() {
        Portfolio portfolio = portfolioService.listPortfolio().get(0);
        List<Position> positions = portfolioService.listPosition(portfolio.getName());
//        BidAskBrokerDTO bidAskBrokerDTO = new BidAskBrokerDTO();
//        tradeTaskService.autoSellForCrown(bidAskBrokerDTO,portfolio,positions);
    }

}
