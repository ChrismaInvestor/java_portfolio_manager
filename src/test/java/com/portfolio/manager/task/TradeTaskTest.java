package com.portfolio.manager.task;

import com.portfolio.manager.constant.Constant;
import com.portfolio.manager.domain.Portfolio;
import com.portfolio.manager.domain.Position;
import com.portfolio.manager.domain.PositionSnapshot;
import com.portfolio.manager.integration.MarketDataClient;
import com.portfolio.manager.integration.OrderPlacementClient;
import com.portfolio.manager.repository.CbStockMappingRepo;
import com.portfolio.manager.service.OrderService;
import com.portfolio.manager.service.PortfolioService;
import com.portfolio.manager.service.PositionSnapshotService;
import com.portfolio.manager.service.sell.CrownSellStrategy;
import com.portfolio.manager.service.sell.VWAP;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@SpringBootTest
public class TradeTaskTest {

    @Resource
    MarketDataClient marketDataClient;

    @Resource
    CbStockMappingRepo cbStockMappingRepo;

    @Resource
    OrderPlacementClient orderPlacementClient;

    @Resource
    PortfolioService portfolioService;

    @Resource
    OrderService orderService;

    @Resource
    PositionSnapshotService positionSnapshotService;


    @Resource
    TradeTask tradeTask;

    @Resource
    VWAP vwap;

    @Test
    public void buyBackForCrown() {
        log.info("whole portfolio stop loss: {}", TradeTask.getWholePortfolioStopLossBar());
        BigDecimal lastNav = new BigDecimal("1");
        BigDecimal currentNav = new BigDecimal("1.01");
        if (currentNav.divide(lastNav, 4, RoundingMode.HALF_UP).compareTo(Constant.CROWN_WHOLE_PORTFOLIO_STOP_LOSS_EXCEPTION) > 0 && currentNav.divide(lastNav, 4, RoundingMode.HALF_UP).compareTo(Constant.CROWN_WHOLE_PORTFOLIO_STOP_LOSS) <= 0) {
            log.info("The whole portfolio is reaching stop loss line");
        }

        Portfolio portfolio = portfolioService.getPortfolio("皇冠");
        List<Position> positions = portfolioService.listPosition("皇冠");
        Optional<PositionSnapshot> posSnapShot = positionSnapshotService.get().stream().filter(position -> position.getSecurityCode().equals("128106")).findFirst();
        Optional<Position> pos = positions.stream().filter(position -> position.getSecurityCode().equals("128106")).findFirst();
        if (posSnapShot.isPresent() && pos.isPresent()) {
            log.info("======new stop loss======");
            marketDataClient.getBidAsk(List.of("128106")).forEach(
                    bidAskBrokerDTO -> tradeTask.handleStopLossMultiTier(pos.get(), posSnapShot.get(), portfolio, bidAskBrokerDTO,"Stop hit")
            );
        }

//        var codes = List.of("118019","113615", "123106", "113516", "113534", "128042");
        var codes = List.of("123118","123106","128042");
        log.info("account info: {}", orderPlacementClient.queryAcct());
        Map<String, CrownSellStrategy> cbSellStrategyMapping = new ConcurrentHashMap<>();
        for (int i = 0; i < 3; i++) {
            marketDataClient.getBidAsk(codes).forEach(
                    bidAskBrokerDTO -> {
                        log.info("is sellable: {}", tradeTask.isSellable(bidAskBrokerDTO));
                        log.info("is slump: {}", tradeTask.isSlump(bidAskBrokerDTO));
                        tradeTask.updateVWAP();

//                    log.info("price: {}", bidAskBrokerDTO);
                    if (cbSellStrategyMapping.containsKey(bidAskBrokerDTO.securityCode())) {
                        var strategy = cbSellStrategyMapping.get(bidAskBrokerDTO.securityCode());
                        strategy.updateState(bidAskBrokerDTO);
                    } else {
                        CrownSellStrategy strategy = new CrownSellStrategy(marketDataClient, cbStockMappingRepo, vwap);
                        strategy.updateState(bidAskBrokerDTO);
                        cbSellStrategyMapping.put(bidAskBrokerDTO.securityCode(), strategy);
                    }
//                    log.info("strategy map: {}", cbSellStrategyMapping.get(bidAskBrokerDTO.securityCode()));
                    });
        }
//        log.info("cash: {}", orderPlacementClient.checkCash());
//
//        SubOrder subOrder = new SubOrder();
//        subOrder.setSecurityCode("600519");
//        subOrder.setBuyOrSell(Direction.买入);
//        subOrder.setId(100L);
//        orderService.execute(subOrder,1L, 1634.03d, 100000);
//        BigDecimal bg = BigDecimal.valueOf(0.974d);
//        log.info(" compare: {}", bg.compareTo(TradeTask.getStopLossBar()) > 0);
//        List<String> codes = List.of("113566","600898","113588","128066");
//        marketDataClient.getBidAsk(codes).forEach(
//                bidAskBrokerDTO -> {
//                    if (BigDecimal.valueOf(bidAskBrokerDTO.bidPrice1()).divide(BigDecimal.valueOf(bidAskBrokerDTO.lastClose()), 4, RoundingMode.HALF_EVEN).compareTo(Constant.CROWN_TAKE_PROFIT) >= 0 ||
//                            BigDecimal.valueOf(bidAskBrokerDTO.high()).divide(BigDecimal.valueOf(bidAskBrokerDTO.lastClose()), 4, RoundingMode.HALF_EVEN).compareTo(Constant.CROWN_TAKE_PROFIT) >= 0 ||
//                            BigDecimal.valueOf(bidAskBrokerDTO.askPrice1()).divide(BigDecimal.valueOf(bidAskBrokerDTO.lastClose()), 4, RoundingMode.HALF_EVEN).compareTo(Constant.CROWN_STOP_LOSS) <= 0) {
//                        log.info("hit...{}", bidAskBrokerDTO);
//                    }
//                });
    }
}
