package com.portfolio.manager.service;

import com.portfolio.manager.domain.Order;
import com.portfolio.manager.domain.SubOrder;
import com.portfolio.manager.integration.BidAskService;
import com.portfolio.manager.repository.SubOrderRepo;
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
    BidAskService bidAskService;

    @Override
    public List<SubOrder> testSplitOrders(Order order, LocalDateTime startTime) {

        LocalDateTime endTime = startTime.plusHours(1L);
        LocalDateTime halfCourtTime = LocalDateTime.of(LocalDate.now(), LocalTime.of(11, 3));
//        if (endTime.isAfter(halfCourtTime)) {
//            endTime = halfCourtTime;
//        }
        long minutes = Duration.between(startTime, endTime).toMinutes();

        List<SubOrder> subOrders = new ArrayList<>();
        splitEven(subOrders, BigDecimal.valueOf(order.getPlannedShare()).divide(BigDecimal.valueOf(100L), RoundingMode.UNNECESSARY).longValue(), minutes);
        for (int i = 0; i < subOrders.size(); i++) {
            subOrders.get(i).setStartTime(startTime.plusMinutes(i));
            subOrders.get(i).setEndTime(startTime.plusMinutes(i + 1));
        }
        log.info("Code: {}, Suborders: {}", order.getSecurityCode(), subOrders);
        subOrderRepo.saveAll(subOrders);
        return subOrders;
    }

    @Override
    public void execute(SubOrder order) {

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
