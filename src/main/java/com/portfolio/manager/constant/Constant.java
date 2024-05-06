package com.portfolio.manager.constant;

import java.math.BigDecimal;

public class Constant {

    public static final long STOCK_MULTIPLE = 100L;

    public static final long CONVERTIBLE_BOND_MULTIPLE = 10L;

    public static final BigDecimal CROWN_TAKE_PROFIT = new BigDecimal("1.075");

    public static final BigDecimal CROWN_STOP_LOSS = new BigDecimal("0.975");

    public static final BigDecimal CROWN_LOCK_PROFIT = new BigDecimal("1.035");

    public static final BigDecimal CROWN_MAX_DRAW_DOWN =new BigDecimal("0.04");

    public static final BigDecimal CROWN_LET_PROFIT_RUN_TAKE_PROFIT = new BigDecimal("1.065");

    public static final BigDecimal CROWN_WHOLE_PORTFOLIO_STOP_LOSS = BigDecimal.valueOf(0.9825d);
}
