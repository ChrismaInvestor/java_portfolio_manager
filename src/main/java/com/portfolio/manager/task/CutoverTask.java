package com.portfolio.manager.task;

import com.portfolio.manager.domain.Dynamics;
import com.portfolio.manager.domain.Investor;
import com.portfolio.manager.domain.Nav;
import com.portfolio.manager.domain.Portfolio;
import com.portfolio.manager.domain.strategy_specific.PositionBookForCrown;
import com.portfolio.manager.integration.MarketDataClient;
import com.portfolio.manager.notification.Notification;
import com.portfolio.manager.repository.InvestorRepo;
import com.portfolio.manager.repository.NavRepo;
import com.portfolio.manager.repository.PositionBookForCrownRepo;
import com.portfolio.manager.service.PortfolioService;
import com.portfolio.manager.service.PositionSnapshotService;
import com.portfolio.manager.service.tracking.CovInfoService;
import com.portfolio.manager.service.tracking.SecurityToTrack;
import com.portfolio.manager.strategy.CovShakingStrategy;
import com.portfolio.manager.util.Util;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.BooleanSupplier;

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

    @Resource
    PositionSnapshotService positionSnapshotService;

    @Resource
    CovInfoService covInfoService;

    @Resource
    MarketDataClient marketDataClient;

    @Scheduled(cron = "59 00 15 ? * MON-FRI")
    @Scheduled(cron = "59 30 11 ? * MON-FRI")
    public void sendNav() {
        portfolioService.listNavs().stream().filter(nav -> nav.getPortfolioName().equals("皇冠")).forEach(nav -> wechatPublicAccount.send(nav.getPortfolioName(), nav.getNav().toString()));
    }

//    @Scheduled(cron = "50 29 09 ? * MON-FRI")
//    @Scheduled(cron = "50 51 20 ? * MON-SUN")
    public void covShaking() throws InterruptedException {
        Set<String> blockedList = new HashSet<>();
        LocalTime threePM = LocalTime.of(14, 0);
        while (LocalTime.now().isBefore(threePM)) {
            log.info("surveillance start");
            CovShakingStrategy strategy = new CovShakingStrategy(covInfoService, marketDataClient, 50000L, blockedList);

            BooleanSupplier buyCondition = () -> strategy.getTargetCov() != null;

            ScheduledExecutorService buyScheduler = Executors.newSingleThreadScheduledExecutor();
            ScheduledExecutorService sellScheduler = Executors.newSingleThreadScheduledExecutor();

            CountDownLatch latch = new CountDownLatch(1);
            Util.newTimedScheduledJob(strategy::buy, buyCondition, 2, 120, buyScheduler, latch);
            latch.await();
            log.info("count down: {}", latch.getCount());
            LocalTime buyCloseTime = LocalTime.now().plusMinutes(3L);
            latch = new CountDownLatch(1);
            Util.newTimedScheduledJob(strategy::mockOrderPlacementBuy, () -> false, 1, 3, buyScheduler, latch);
//        latch.await();
            Util.newTimedScheduledJob(strategy::sell, () -> strategy.getIsSellCompletion() || (!LocalTime.now().isBefore(buyCloseTime) && strategy.getTargetCov().getHoldingVol().compareTo(0L) == 0), 1, 360, sellScheduler, latch);
            latch.await();
            blockedList.add(strategy.getTargetCov().getCovCode());
            sellScheduler.shutdown();
            log.info("sell completion");
        }
    }

    @Scheduled(cron = "59 00 15 ? * MON-FRI")
    public void takeSnapshotOfPosition() {
        var positions = portfolioService.listPosition("皇冠");
        positionSnapshotService.update(positions);
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
//                positionBookForCrown.setBuyLock(false);
            });
            positionBookForCrownRepo.saveAll(positionBookForCrownList);

            List<Investor> investors = investorRepo.findAll();
            Map<String, BigDecimal> portfolioSharesMap = Util.getPortfolioSharesMap(investors);
            Nav nav = new Nav();
            nav.setPortfolioName(portfolioDTO.name());
            nav.setNav(BigDecimal.valueOf(dynamics.getTotalMarketValue()).divide(portfolioSharesMap.get(portfolioDTO.name()), 6, RoundingMode.DOWN));
            navRepo.save(nav);
            //清楚当日Sell Lock
            TradeTask.sellLockSet.clear();
            TradeTask.cbSellStrategyMapping.clear();
        });

        //Set the vols to 0
        TradeTask.securityToTrackMap.forEach((securityCode, securityToCheck) -> TradeTask.securityToTrackMap.put(securityCode, new SecurityToTrack(securityCode, 0L, securityToCheck.states())));
    }
}
