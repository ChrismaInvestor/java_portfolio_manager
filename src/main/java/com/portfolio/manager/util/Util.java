package com.portfolio.manager.util;

import com.portfolio.manager.domain.Investor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

@Slf4j
public class Util {

    public static Map<String, BigDecimal> getPortfolioSharesMap(List<Investor> investors) {
        Map<String, BigDecimal> portfolioSharesMap = new HashMap<>();

        investors.forEach(investor -> {
            if (portfolioSharesMap.containsKey(investor.getPortfolioName())) {
                var newResult = portfolioSharesMap.get(investor.getPortfolioName()).add(investor.getShareAmount());
                portfolioSharesMap.put(investor.getPortfolioName(), newResult);
            } else {
                portfolioSharesMap.put(investor.getPortfolioName(), investor.getShareAmount());
            }
        });

        return portfolioSharesMap;
    }

    public static Long calVolume(Long volume, Double discount, Long multiple) {
        BigDecimal multipleBd = BigDecimal.valueOf(multiple);
        BigDecimal dividedVol = BigDecimal.valueOf(volume).divide(multipleBd, RoundingMode.UNNECESSARY);
        return dividedVol.multiply(BigDecimal.valueOf(discount)).setScale(0, RoundingMode.UP).multiply(multipleBd).longValue();
    }

    public static boolean isTradingDay() {
        LocalDate today = LocalDate.now();
        return !today.getDayOfWeek().equals(DayOfWeek.SUNDAY) && !today.getDayOfWeek().equals(DayOfWeek.SATURDAY);
    }

    public static BigDecimal priceMovementDivide(Double numerator, Double denominator) {
        return BigDecimal.valueOf(numerator).divide(BigDecimal.valueOf(denominator), 4, RoundingMode.HALF_EVEN);
    }

    public static void newTimedScheduledJob(Runnable task, BooleanSupplier exitCondition, long intervalSecond, long durationInMinutes, ScheduledExecutorService scheduler, CountDownLatch latch) {
        // 设置任务的初始延迟和执行间隔
        long initialDelay = 0; // 初始延迟为 0 秒，立即开始执行
        long endTime = System.currentTimeMillis() + durationInMinutes * 60 * 1000;
        // 调用 scheduleAtFixedRate 方法启动任务
        scheduler.scheduleAtFixedRate(() -> {
            if (System.currentTimeMillis() >= endTime) {
                scheduler.shutdown(); // 任务结束，关闭线程池
            } else if (exitCondition.getAsBoolean()) {
                latch.countDown();
            } else {
                task.run(); // 执行任务逻辑
            }
        }, initialDelay, intervalSecond, TimeUnit.SECONDS);
    }
}
