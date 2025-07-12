package com.portfolio.manager.service.tracking;

import com.portfolio.manager.service.new_sell.State;

import java.math.BigDecimal;
import java.util.List;
import java.util.Queue;

public record SecurityToTrack(String stockCode, Long vol, Queue<State> states) {
}
