package com.portfolio.manager.service;

import com.portfolio.manager.domain.Position;
import com.portfolio.manager.domain.PositionSnapshot;
import com.portfolio.manager.repository.PositionSnapshotRepo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PositionSnapshotServiceImpl implements PositionSnapshotService {
    @Resource
    PositionSnapshotRepo positionSnapshotRepo;

    @Override
    public void update(List<Position> positions) {
        positionSnapshotRepo.deleteAll();
        List<PositionSnapshot> ans = positions.stream().map(position -> {
            PositionSnapshot positionSnapshot = new PositionSnapshot();
            positionSnapshot.setSecurityCode(position.getSecurityCode());
                    positionSnapshot.setSecurityShare(position.getSecurityShare());
            return positionSnapshot;
        }).toList();
        positionSnapshotRepo.saveAll(ans);
    }

    @Override
    public List<PositionSnapshot> get() {
        return positionSnapshotRepo.findAll();
    }
}
