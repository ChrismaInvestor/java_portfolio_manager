package com.portfolio.manager.dto;

import java.util.List;

public record PositionDTO(String portfolio, List<SecurityDTO> positions) {
}
