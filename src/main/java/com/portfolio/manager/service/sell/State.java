package com.portfolio.manager.service.sell;

import com.portfolio.manager.constant.Constant;
import com.portfolio.manager.dto.BidAskBrokerDTO;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedDeque;

@Slf4j
public abstract class State {
    ConcurrentLinkedDeque<BigDecimal> bid1PricesSlidingWindow = new ConcurrentLinkedDeque<>();

    abstract void updateState(BidAskBrokerDTO bidAskBrokerDTO);

    abstract boolean isSellable();

    boolean isSlump(BidAskBrokerDTO bidAskBrokerDTO) {
        if (bid1PricesSlidingWindow.isEmpty()){
            return false;
        }
        var max = this.findMax(bid1PricesSlidingWindow);
//        var min = this.findMin(bid1PricesSlidingWindow);
        log.info("max: {}", max);
        return false;
//        return Util.priceMovementDivide(max.subtract(min).doubleValue(), bidAskBrokerDTO.lastClose()).compareTo(Constant.CROWN_MAX_DRAW_DOWN) <= 0;
    }

    void updateBid1PricesSlidingWindow(BidAskBrokerDTO bidAskBrokerDTO){
//        while (!bid1PricesSlidingWindow.isEmpty() && BigDecimal.valueOf(bidAskBrokerDTO.bidPrice1()).compareTo(bid1PricesSlidingWindow.peekLast()) >= 0) {
//            bid1PricesSlidingWindow.pollLast();
//        }
        this.bid1PricesSlidingWindow.offer(BigDecimal.valueOf(bidAskBrokerDTO.bidPrice1()));
        while (bid1PricesSlidingWindow.size() > Constant.SLUMP_MAX_SECONDS){
            bid1PricesSlidingWindow.poll();
        }
    }

    public String toString() {
        return this.getClass().getName();
    }

    private BigDecimal findMax(ConcurrentLinkedDeque<BigDecimal> deque) {
        Iterator<BigDecimal> iterator = deque.descendingIterator();
        BigDecimal max = deque.peekFirst();
        if (deque.size() == 1){
            return max;
        }
        while (iterator.hasNext()) {
            BigDecimal value = iterator.next();
            if (value.compareTo(max) >= 0) {
                max = value;
            }
        }
        return max;
    }

    private BigDecimal findMin(ConcurrentLinkedDeque<BigDecimal> deque, BigDecimal max) {
        BigDecimal min = max;
        if (deque.size() == 1){
            return max;
        }

        Iterator<BigDecimal> iterator = deque.descendingIterator();
        boolean start = false;
        while (iterator.hasNext()) {
            BigDecimal value = iterator.next();
            if (value.compareTo(min) < 0) {
                min = value;
            }
        }
        return min;
    }

}
