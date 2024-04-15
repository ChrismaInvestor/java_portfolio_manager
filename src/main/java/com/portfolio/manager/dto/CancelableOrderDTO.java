package com.portfolio.manager.dto;

public record CancelableOrderDTO(Integer cancelableVolume, Long orderId, String securityCode) {}