package com.portfolio.manager.factory;

import com.portfolio.manager.domain.Direction;
import com.portfolio.manager.domain.Order;
import com.portfolio.manager.domain.SubOrder;
import com.portfolio.manager.domain.Trade;
import com.portfolio.manager.dto.integration.BidAskBrokerDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service("NormalOrderExecutionFactory")
public class NormalOrderExecutionFactory extends OrderExecutionFactory{

    @Override
    public void executeOrders(String portfolioName) {
        //Order execution
        List<Order> orders = orderService.listPendingOrders(portfolioName);
        Set<String> securityCodes = orders.stream().flatMap(order -> order.getSubOrders().stream().filter(subOrder -> this.isBetween(subOrder.getStartTime(), subOrder.getEndTime()) && subOrder.getRemainingShare() > 0)
                .map(SubOrder::getSecurityCode)).collect(Collectors.toSet());

        if (!securityCodes.isEmpty()) {
            Map<String, BidAskBrokerDTO> bidAsks = marketDataClient.getBidAsk(securityCodes.stream().toList()).stream().collect(Collectors.toMap(BidAskBrokerDTO::securityCode, Function.identity()));
            log.info("bid ask: {}", bidAsks);
            orders.stream().parallel().forEach(order -> {
                var subOrders = order.getSubOrders().stream().filter(subOrder -> this.isBetween(subOrder.getStartTime(), subOrder.getEndTime()) && subOrder.getRemainingShare() > 0).toList();
                if (!subOrders.isEmpty()) {
                    subOrders.stream().parallel().forEach(subOrder -> {
                        if (order.getBuyOrSell().equals(Direction.买入)) {
                            orderService.execute(subOrder, order.getId(), bidAsks.get(order.getSecurityCode()).askPrice1(),
                                    Math.min(subOrder.getRemainingShare().intValue(), bidAsks.get(order.getSecurityCode()).askVol1()));
                        } else if (order.getBuyOrSell().equals(Direction.卖出)) {
                            orderService.execute(subOrder, order.getId(), bidAsks.get(order.getSecurityCode()).bidPrice1(), Math.min(subOrder.getRemainingShare().intValue(), bidAsks.get(order.getSecurityCode()).bidVol1()));
                        }
                    });
                }
            });
        }

        var cancelableOrders = orderPlacementClient.queryCancelableOrders();
        cancelableOrders.forEach(cancelableOrder -> {
            Optional<Trade> trade = tradeRepo.findByClientOrderIdOrderByCreateTimeDesc(cancelableOrder.orderId()).stream().findFirst();
            trade.ifPresent(item -> {
                if (item.getCreateTime().plusSeconds(30L).isBefore(LocalDateTime.now())) {
                    boolean result = orderPlacementClient.cancelOrder(cancelableOrder.orderId());
                    if (result) {
                        Optional<SubOrder> subOrder = subOrderRepo.findById(item.getSubOrderId());
                        subOrder.ifPresent(subOrderItem -> {
                            subOrderItem.setRemainingShare(Long.valueOf(cancelableOrder.cancelableVolume()));
                            subOrderItem.setEndTime(LocalDateTime.now().plusMinutes(1L));
                            subOrderRepo.save(subOrderItem);
                        });
                        item.setVolume(item.getVolume() - cancelableOrder.cancelableVolume());
                        tradeRepo.save(item);
                    }
                }
            });
        });
    }
}
