package com.portfolio.manager.task;

import com.portfolio.manager.constant.Constant;
import com.portfolio.manager.domain.Direction;
import com.portfolio.manager.domain.Position;
import com.portfolio.manager.domain.strategy_specific.PositionBookForCrown;
import com.portfolio.manager.dto.OrderDTO;
import com.portfolio.manager.repository.PositionBookForCrownRepo;
import com.portfolio.manager.util.Util;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@SpringBootTest
public class TradeTaskTest {
    @Resource
    PositionBookForCrownRepo positionBookForCrownRepo;

    @Test
    public void buyBackForCrown() {
        double buyBackDiscount = 0.5d;

        List<PositionBookForCrown> positionBook = positionBookForCrownRepo.findByPortfolioName("皇冠");
        log.info("positionbook: {}", positionBook);
        List<Position> positions = new ArrayList<>();
        Position positionTest = new Position();
        positionTest.setSecurityCode("113504");
        positionTest.setSecurityShare(840L);
        positions.add(positionTest);
        Map<String, Position> position = positions.stream().collect(Collectors.toMap(Position::getSecurityCode, Function.identity()));
        positionBook.stream().parallel().forEach(positionBookForCrown -> {
            if (positionBookForCrown.getBuyBack()) {
                if (position.get(positionBookForCrown.getSecurityCode()) == null) {
                    positionBookForCrown.setSecurityShare(Util.calVolume(positionBookForCrown.getSecurityShare(), buyBackDiscount, Constant.CONVERTIBLE_BOND_MULTIPLE));
                    log.warn("Buy back hit: {}", positionBookForCrown);
                    OrderDTO orderDTO = new OrderDTO(Direction.买入, positionBookForCrown.getSecurityShare(), positionBookForCrown.getSecurityName(), positionBookForCrown.getSecurityCode(), 0.0d);
                    log.warn("new order: {}", orderDTO);
                } else if (position.get(positionBookForCrown.getSecurityCode()).getSecurityShare().compareTo(positionBookForCrown.getSecurityShare()) < 0) {
                    log.warn("Buy back hit: {}, difference: {}", positionBookForCrown, positionBookForCrown.getSecurityShare() - position.get(positionBookForCrown.getSecurityCode()).getSecurityShare());
                    OrderDTO orderDTO = new OrderDTO(Direction.买入, positionBookForCrown.getSecurityShare() - position.get(positionBookForCrown.getSecurityCode()).getSecurityShare(), positionBookForCrown.getSecurityName(), positionBookForCrown.getSecurityCode(), 0.0d);
                    log.warn("new order: {}", orderDTO);
                }
            }
        });
    }
}
