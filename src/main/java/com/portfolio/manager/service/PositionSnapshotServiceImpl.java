package com.portfolio.manager.service;

import com.portfolio.manager.domain.Direction;
import com.portfolio.manager.domain.Position;
import com.portfolio.manager.domain.PositionSnapshot;
import com.portfolio.manager.dto.OrderDTO;
import com.portfolio.manager.repository.PositionSnapshotRepo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class PositionSnapshotServiceImpl implements PositionSnapshotService {
    @Resource
    PositionSnapshotRepo positionSnapshotRepo;

    @Override
    public void update(List<Position> positions, List<OrderDTO> orders) {
        positionSnapshotRepo.deleteAll();
        Map<String, OrderDTO> map = orders.stream().collect(Collectors.toMap(OrderDTO::securityCode, Function.identity()));
        List<PositionSnapshot> ans = new ArrayList<>();
        List<PositionSnapshot> positionSnapshots = positions.stream().map(position -> {
            PositionSnapshot positionSnapshot = new PositionSnapshot();
            positionSnapshot.setSecurityCode(position.getSecurityCode());
            if (map.containsKey(position.getSecurityCode())) {
                OrderDTO order = map.get(position.getSecurityCode());
                if (order.buyOrSell().equals(Direction.买入)) {
                    positionSnapshot.setSecurityShare(position.getSecurityShare() + order.share());
                } else if (order.buyOrSell().equals(Direction.卖出)) {
                    positionSnapshot.setSecurityShare(position.getSecurityShare() - order.share());
                }
            } else {
                positionSnapshot.setSecurityShare(position.getSecurityShare());
            }
            return positionSnapshot;
        }).toList();
        Set<String> codes = positionSnapshots.stream().map(PositionSnapshot::getSecurityCode).collect(Collectors.toSet());
        orders.forEach(order -> {
            if (!codes.contains(order.securityCode()) && order.buyOrSell().equals(Direction.买入)) {
                PositionSnapshot positionSnapshot = new PositionSnapshot();
                positionSnapshot.setSecurityCode(order.securityCode());
                positionSnapshot.setSecurityShare(order.share());
                ans.add(positionSnapshot);
            }
        });
        ans.addAll(positionSnapshots.stream().filter(positionSnapshot -> positionSnapshot.getSecurityShare() > 0).toList());
        positionSnapshotRepo.saveAll(ans);
    }

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
