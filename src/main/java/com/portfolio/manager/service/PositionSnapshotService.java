package com.portfolio.manager.service;

import com.portfolio.manager.domain.Position;
import com.portfolio.manager.domain.PositionSnapshot;
import com.portfolio.manager.dto.OrderDTO;

import java.util.List;

public interface PositionSnapshotService {
    @Deprecated
    void update(List<Position> positions, List<OrderDTO> orders);

    void update(List<Position> positions);
    List<PositionSnapshot> get();
}
