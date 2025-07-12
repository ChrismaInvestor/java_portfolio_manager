package com.portfolio.manager.service.tracking;

import com.portfolio.manager.data.PositionData;
import com.portfolio.manager.dto.integration.PositionBrokerDTO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class TrackingServiceImpl implements TrackingService{
    @Resource
    PositionData positionData;

    @Override
    public PositionBrokerDTO queryPosition(String securityCode) {
        return positionData.getMap().get(securityCode);
    }
}
