package com.portfolio.manager.task;

import com.portfolio.manager.domain.Dynamics;
import com.portfolio.manager.domain.Investor;
import com.portfolio.manager.domain.Nav;
import com.portfolio.manager.domain.Portfolio;
import com.portfolio.manager.domain.strategy_specific.PositionBookForCrown;
import com.portfolio.manager.notification.Notification;
import com.portfolio.manager.repository.InvestorRepo;
import com.portfolio.manager.repository.NavRepo;
import com.portfolio.manager.repository.PositionBookForCrownRepo;
import com.portfolio.manager.service.PortfolioService;
import com.portfolio.manager.util.Util;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class CutoverTask {
    @Resource
    PortfolioService portfolioService;

    @Resource
    PositionBookForCrownRepo positionBookForCrownRepo;

    @Resource
    InvestorRepo investorRepo;

    @Resource
    NavRepo navRepo;

    @Resource
    Notification wechatPublicAccount;

    @Scheduled(cron = "59 00 15 ? * MON-FRI")
    @Scheduled(cron = "59 30 11 ? * MON-FRI")
    public void sendNav() {
        portfolioService.listPortfolioDTO().forEach(portfolioDTO -> {
            Portfolio portfolio = portfolioService.getPortfolio(portfolioDTO.name());
            Dynamics dynamics = portfolioService.getDynamics(portfolio);

            List<Investor> investors = investorRepo.findAll();
            Map<String, BigDecimal> portfolioSharesMap = Util.getPortfolioSharesMap(investors);
            Nav nav = new Nav();
            nav.setPortfolioName(portfolioDTO.name());
            nav.setNav(BigDecimal.valueOf(dynamics.getTotalMarketValue()).divide(portfolioSharesMap.get(portfolioDTO.name()), 6, RoundingMode.DOWN));
            wechatPublicAccount.send(nav.getPortfolioName(), nav.getNav().toString());
        });
    }

    @Scheduled(cron = "0 59 23 ? * MON-FRI")
    public void cashRefresh() {
        portfolioService.listPortfolioDTO().forEach(portfolioDTO -> {
            Portfolio portfolio = portfolioService.getPortfolio(portfolioDTO.name());
            Dynamics dynamics = portfolioService.getDynamics(portfolio);
            dynamics.setLastDayCash(dynamics.getCash());
            portfolioService.updateDynamics(dynamics);

            List<PositionBookForCrown> positionBookForCrownList = positionBookForCrownRepo.findByPortfolioName(portfolio.getName());
            positionBookForCrownList.forEach(positionBookForCrown -> {
                positionBookForCrown.setSellLock(false);
                positionBookForCrown.setBuyLock(false);
            });
            positionBookForCrownRepo.saveAll(positionBookForCrownList);

            List<Investor> investors = investorRepo.findAll();
            Map<String, BigDecimal> portfolioSharesMap = Util.getPortfolioSharesMap(investors);
            Nav nav = new Nav();
            nav.setPortfolioName(portfolioDTO.name());
            nav.setNav(BigDecimal.valueOf(dynamics.getTotalMarketValue()).divide(portfolioSharesMap.get(portfolioDTO.name()), 6, RoundingMode.DOWN));
            navRepo.save(nav);
        });
    }
}
