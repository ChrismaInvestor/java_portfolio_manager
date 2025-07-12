package com.portfolio.manager.data;

import com.portfolio.manager.dto.integration.BidAskBrokerDTO;
import com.portfolio.manager.dto.integration.PositionBrokerDTO;
import com.portfolio.manager.integration.OrderPlacementClient;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class PositionData implements IData{
    @Resource
    OrderPlacementClient orderPlacemenClient;

    List<PositionBrokerDTO> positionList = new ArrayList<>();

    @Override
    public Map<String, PositionBrokerDTO> getMap() {
        return positionList.stream().collect(Collectors.toMap(PositionBrokerDTO::code, Function.identity()));
    }

    @Override
    public void update() {
        positionList = orderPlacemenClient.queryAllPositions();
    }
}
