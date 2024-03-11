package com.portfolio.manager.service;

import com.portfolio.manager.constant.Constant;
import com.portfolio.manager.domain.Direction;
import com.portfolio.manager.domain.Order;
import com.portfolio.manager.domain.SubOrder;
import com.portfolio.manager.domain.Trade;
import com.portfolio.manager.integration.OrderPlacementClient;
import com.portfolio.manager.repository.SubOrderRepo;
import com.portfolio.manager.repository.TradeRepo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class VWAPEven implements AlgoService {
    @Resource
    SubOrderRepo subOrderRepo;

    @Override
    public List<SubOrder> splitOrders(Order order, LocalDateTime startTime, LocalDateTime endTime) {
        long minutes = Duration.between(startTime, endTime).toMinutes();

        List<SubOrder> subOrders = new ArrayList<>();
        long multiple = order.getSecurityCode().startsWith("11") || order.getSecurityCode().startsWith("12") ? Constant.CONVERTIBLE_BOND_MULTIPLE : Constant.STOCK_MULTIPLE;
        this.splitEven(subOrders, BigDecimal.valueOf(order.getPlannedShare()).divide(BigDecimal.valueOf(multiple), RoundingMode.UNNECESSARY).longValue(), minutes, multiple);
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


    private void splitEven(List<SubOrder> subOrders, Long remainingAmount, Long remainingMinutes, Long multiple) {
        if (remainingMinutes == 0 || remainingAmount == 0) {
            return;
        }
        long amount = BigDecimal.valueOf(remainingAmount).divide(BigDecimal.valueOf(remainingMinutes), RoundingMode.UP).longValue();
        SubOrder subOrder = new SubOrder();
        subOrder.setPlannedShare(amount * multiple);
        subOrder.setRemainingShare(amount * multiple);
        subOrders.add(subOrder);
        splitEven(subOrders, remainingAmount - amount, remainingMinutes - 1, multiple);
    }
}