package com.portfolio.manager.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record NavDTO(LocalDate date, BigDecimal nav, String portfolioName) {
}
