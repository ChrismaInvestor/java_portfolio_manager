package com.portfolio.manager.service;

import com.portfolio.manager.domain.Order;
import com.portfolio.manager.domain.SubOrder;
import com.portfolio.manager.domain.Trade;
import com.portfolio.manager.repository.SubOrderRepo;
import com.portfolio.manager.repository.TradeRepo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class AlgoServiceImpl implements AlgoService {
    @Resource
    SubOrderRepo subOrderRepo;

    @Resource
    TradeRepo tradeRepo;

    @Override
    public List<SubOrder> testSplitOrders(Order order, LocalDateTime startTime) {
        LocalDateTime endTime = startTime.plusMinutes(10L);
//        LocalDateTime endTime = startTime.plusHours(1L);
        LocalDateTime halfCourtTime = LocalDateTime.of(LocalDate.now(), LocalTime.of(11, 29));
//        if (endTime.isAfter(halfCourtTime)) {
//            endTime = halfCourtTime;
//        }
        long minutes = Duration.between(startTime, endTime).toMinutes();

        List<SubOrder> subOrders = new ArrayList<>();
        splitEven(subOrders, BigDecimal.valueOf(order.getPlannedShare()).divide(BigDecimal.valueOf(100L), RoundingMode.UNNECESSARY).longValue(), minutes);
        for (int i = 0; i < subOrders.size(); i++) {
            subOrders.get(i).setStartTime(startTime.plusMinutes(i));
            subOrders.get(i).setEndTime(startTime.plusMinutes(i + 1));
            subOrders.get(i).setBuyOrSell(order.getBuyOrSell());
            subOrders.get(i).setSecurityCode(order.getSecurityCode());
        }
        log.info("Code: {}, Suborders: {}", order.getSecurityCode(), subOrders);
        subOrderRepo.saveAll(subOrders);
        return subOrders;
    }

    @Override
    public void execute(SubOrder order, Long orderId, Double price, Integer vol) {
        Trade trade = new Trade();
        trade.setCode(order.getSecurityCode());
        trade.setDirection(order.getBuyOrSell());
        trade.setOrderId(orderId);
//       执行下单开始



//        执行下单结束
        trade.setPrice(price);
        trade.setVolume(Long.valueOf(vol));
//        if (order.getBuyOrSell().equals("买入")) {
//            trade.setPrice(bidAskDTO.bidPrice().get(0));
//            if (bidAskDTO.bidVol().get(0).longValue() >= order.getRemainingShare()) {
//                trade.setVolume(order.getRemainingShare());
//            } else {
//                trade.setVolume(bidAskDTO.bidVol().get(0).longValue());
//            }
//        } else {
//            trade.setPrice(bidAskDTO.askPrice().get(0));
//            if (bidAskDTO.askVol().get(0).longValue() >= order.getRemainingShare()) {
//                trade.setVolume(order.getRemainingShare());
//            } else {
//                trade.setVolume(bidAskDTO.askVol().get(0).longValue());
//            }
//        }
        order.setRemainingShare(order.getRemainingShare() - trade.getVolume());
        tradeRepo.save(trade);
        subOrderRepo.save(order);
    }

    private void splitEven(List<SubOrder> subOrders, Long remainingAmount, Long remainingMinutes) {
        if (remainingMinutes == 0 || remainingAmount == 0) {
            return;
        }
        long amount = BigDecimal.valueOf(remainingAmount).divide(BigDecimal.valueOf(remainingMinutes), RoundingMode.UP).longValue();
        SubOrder subOrder = new SubOrder();
        subOrder.setPlannedShare(amount * 100);
        subOrder.setRemainingShare(amount * 100);
        subOrders.add(subOrder);
        splitEven(subOrders, remainingAmount - amount, remainingMinutes - 1);
    }
}