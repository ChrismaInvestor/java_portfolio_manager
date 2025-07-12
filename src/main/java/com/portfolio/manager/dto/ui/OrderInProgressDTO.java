package com.portfolio.manager.dto.ui;

public record OrderInProgressDTO(String buyOrSell, String securityName, String securityCode, Integer ratio) {
}
