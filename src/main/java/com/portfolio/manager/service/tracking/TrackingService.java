package com.portfolio.manager.service.tracking;

import com.portfolio.manager.dto.integration.PositionBrokerDTO;

public interface TrackingService {
    PositionBrokerDTO queryPosition(String securityCode);

}
