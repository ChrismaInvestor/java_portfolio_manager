package com.portfolio.manager.web;

import com.portfolio.manager.service.new_sell.NormalState;
import com.portfolio.manager.service.new_sell.State;
import com.portfolio.manager.service.new_sell.StopLossState;
import com.portfolio.manager.service.tracking.SecurityToTrack;
import com.portfolio.manager.task.TradeTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

@Slf4j
@RequestMapping("/tracker")
@RestController
public class TrackerController {

    @GetMapping("one")
    public String clear(@RequestParam(name = "stockCode") String stockCode, @RequestParam(name = "tier1StopPct") String tier1StopPct) {
        Queue<State> states = new ConcurrentLinkedDeque<>();
        var tier1 = new BigDecimal(tier1StopPct);
        states.add(new StopLossState(tier1));
        states.add(new NormalState(new BigDecimal(tier1StopPct), null));
        TradeTask.securityToTrackMap.put(stockCode, new SecurityToTrack(stockCode, 0L, states));
        return stockCode + " " + new BigDecimal(tier1StopPct);
    }

    @GetMapping("two")
    public String clear(@RequestParam(name = "stockCode") String stockCode, @RequestParam(name = "tier1StopPct") String tier1StopPct, @RequestParam(name = "tier2StopPct") String tier2StopPct) {
        Queue<State> states = new ConcurrentLinkedDeque<>();
        var tier1 = new BigDecimal(tier1StopPct);
        states.add(new StopLossState(tier1));
        states.add(new NormalState(new BigDecimal(tier1StopPct), new BigDecimal(tier2StopPct)));
        states.add(new NormalState(new BigDecimal(tier2StopPct), null));
        TradeTask.securityToTrackMap.put(stockCode, new SecurityToTrack(stockCode, 0L, states));
        return stockCode + " " + new BigDecimal(tier1StopPct) + " " + new BigDecimal(tier2StopPct);
    }

    @GetMapping
    public Map<String, SecurityToTrack> getSecurityToCheckMap() {
        log.info("{}", TradeTask.securityToTrackMap);
        return TradeTask.securityToTrackMap;
    }

}
