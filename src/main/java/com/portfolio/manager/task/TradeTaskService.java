package com.portfolio.manager.task;

import com.portfolio.manager.constant.Constant;
import com.portfolio.manager.domain.Portfolio;
import com.portfolio.manager.domain.Position;
import com.portfolio.manager.domain.strategy_specific.PositionBookForCrown;
import com.portfolio.manager.dto.BidAskBrokerDTO;
import com.portfolio.manager.dto.BidAskDTO;
import com.portfolio.manager.dto.OrderDTO;
import com.portfolio.manager.repository.PositionBookForCrownRepo;
import com.portfolio.manager.service.OrderService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class TradeTaskService {
    @Resource
    PositionBookForCrownRepo positionBookForCrownRepo;

    @Resource
    OrderService orderService;

    public void autoSellForCrown(BidAskBrokerDTO bidAskBrokerDTO, Portfolio portfolio, List<Position> positions){
        if (BigDecimal.valueOf(bidAskBrokerDTO.bidPrice1()).divide(BigDecimal.valueOf(bidAskBrokerDTO.lastClose()), 4, RoundingMode.HALF_EVEN).compareTo(Constant.CROWN_TAKE_PROFIT) >= 0 ||
                BigDecimal.valueOf(bidAskBrokerDTO.askPrice1()).divide(BigDecimal.valueOf(bidAskBrokerDTO.lastClose()), 4, RoundingMode.HALF_EVEN).compareTo(Constant.CROWN_STOP_LOSS) <= 0) {
            Optional<PositionBookForCrown> book = positionBookForCrownRepo.findByPortfolioNameAndSecurityCode(portfolio.getName(), bidAskBrokerDTO.securityCode());
            if (book.isPresent() && !book.get().getSellLock()) {
                List<OrderDTO> orders = orderService.sell(positions.stream().filter(position -> position.getSecurityCode().equals(bidAskBrokerDTO.securityCode())).toList());
                if (!orders.isEmpty()) {
                    log.warn("BidAsk hit: {}", bidAskBrokerDTO);
                    orderService.addOrder(orders.get(0), portfolio.getName(), LocalDateTime.now(), LocalDateTime.now().plusMinutes(1L));
                    PositionBookForCrown positionBook = book.get();
                    positionBook.setSellLock(true);
                    positionBookForCrownRepo.save(positionBook);
                }
            }
        }
    }
}
