package com.portfolio.manager.dto.ui;

import com.portfolio.manager.dto.integration.SecurityInfoDTO;

import java.util.List;

public record PositionDTO(String portfolio, List<SecurityInfoDTO> positions) {
}
