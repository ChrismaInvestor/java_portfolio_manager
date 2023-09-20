package com.portfolio.manager.dto;

public record OrderInProgressDTO(String buyOrSell, String securityName, String securityCode, Double ratio) {
}
