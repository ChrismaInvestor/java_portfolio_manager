package com.portfolio.manager.task;

import com.portfolio.manager.integration.MarketDataClient;
import com.portfolio.manager.integration.OrderPlacementClient;
import com.portfolio.manager.repository.CbStockMappingRepo;
import com.portfolio.manager.service.OrderService;
import com.portfolio.manager.service.sell.CrownSellStrategy;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;
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
    OrderService orderService;

    @Test
    public void buyBackForCrown() {
        var codes = List.of("113615", "123106", "113516","113534");
        Map<String, CrownSellStrategy> cbSellStrategyMapping = new ConcurrentHashMap<>();
        TradeTask tradeTask = new TradeTask();
        marketDataClient.getBidAsk(codes).forEach(
                bidAskBrokerDTO -> {
                log.info("is sellable: {}", tradeTask.isSellable(bidAskBrokerDTO));
//                    log.info("price: {}", bidAskBrokerDTO);
//                    if (cbSellStrategyMapping.containsKey(bidAskBrokerDTO.securityCode())) {
//                        var strategy = cbSellStrategyMapping.get(bidAskBrokerDTO.securityCode());
//                        strategy.updateState(bidAskBrokerDTO);
//                    } else {
//                        CrownSellStrategy strategy = new CrownSellStrategy(marketDataClient, cbStockMappingRepo);
//                        strategy.updateState(bidAskBrokerDTO);
//                        cbSellStrategyMapping.put(bidAskBrokerDTO.securityCode(), strategy);
//                    }
//                    log.info("strategy map: {}", cbSellStrategyMapping.get(bidAskBrokerDTO.securityCode()));
                });

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
