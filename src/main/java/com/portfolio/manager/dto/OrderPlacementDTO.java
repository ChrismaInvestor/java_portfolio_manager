package com.portfolio.manager.dto;

import java.util.List;

public record OrderPlacementDTO(String portfolio, List<OrderDTO> orders) {
}
