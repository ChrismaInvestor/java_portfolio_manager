package com.portfolio.manager.dto.ui;

import com.portfolio.manager.domain.Direction;

public record OrderDTO(Direction buyOrSell, Long share, String securityName, String securityCode, Double value) {
}
