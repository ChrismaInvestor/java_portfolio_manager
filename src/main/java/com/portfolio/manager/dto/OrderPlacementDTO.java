package com.portfolio.manager.dto;

import java.time.LocalDateTime;
import java.util.List;

public record OrderPlacementDTO(String portfolio, List<OrderDTO> orders, LocalDateTime startTime, LocalDateTime endTime) {
}
