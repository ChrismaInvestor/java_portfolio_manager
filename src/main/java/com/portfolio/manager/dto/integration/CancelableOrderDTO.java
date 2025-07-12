package com.portfolio.manager.dto.integration;

public record CancelableOrderDTO(Integer cancelableVolume, Long orderId, String securityCode) {}