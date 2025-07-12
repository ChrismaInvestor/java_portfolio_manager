package com.portfolio.manager.dto.ui;

import java.time.LocalDateTime;
import java.util.List;

public record OrderPlacementDTO(String portfolio, List<OrderDTO> orders, LocalDateTime startTime, LocalDateTime endTime) {
}
