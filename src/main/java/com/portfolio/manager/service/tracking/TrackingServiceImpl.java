package com.portfolio.manager.service.tracking;

import com.portfolio.manager.data.PositionData;
import com.portfolio.manager.dto.integration.PositionBrokerDTO;
import com.portfolio.manager.integration.OrderPlacementClient;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TrackingServiceImpl implements TrackingService{
    @Resource
    PositionData positionData;

    @Override
    public PositionBrokerDTO queryPosition(String securityCode) {
        return positionData.getMap().get(securityCode);
    }
}
