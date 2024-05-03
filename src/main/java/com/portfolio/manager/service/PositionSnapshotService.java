package com.portfolio.manager.service;

import com.portfolio.manager.domain.Position;
import com.portfolio.manager.domain.PositionSnapshot;

import java.util.List;

public interface PositionSnapshotService {
    void update(List<Position> positions);
    List<PositionSnapshot> get();
}
