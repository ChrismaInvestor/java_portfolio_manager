package com.portfolio.manager.service.new_sell;

import java.math.BigDecimal;

public class StopLossState extends State{

    public StopLossState(BigDecimal upThreshold){
        super(new BigDecimal("0.975"), upThreshold);
    }
}
