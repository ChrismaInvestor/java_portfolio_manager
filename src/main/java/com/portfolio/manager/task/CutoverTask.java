package com.portfolio.manager.task;

import com.portfolio.manager.domain.Dynamics;
import com.portfolio.manager.domain.Portfolio;
import com.portfolio.manager.service.PortfolioService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CutoverTask {
    @Resource
    PortfolioService portfolioService;

    @Scheduled(cron = "0 59 23 ? * MON-FRI")
    public void cashRefresh(){
        portfolioService.listPortfolioDTO().forEach(portfolioDTO -> {
            Portfolio portfolio = portfolioService.getPortfolio(portfolioDTO.name());
            Dynamics dynamics = portfolioService.getDynamics(portfolio);
            dynamics.setLastDayCash(dynamics.getCash());
            portfolioService.updateDynamics(dynamics);
        });
    }
}
