package com.portfolio.manager.dto;

public record OrderDTO(String buyOrSell, Long share, String securityName, String securityCode, Double value) {
}
