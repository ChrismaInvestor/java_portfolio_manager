package com.portfolio.manager.service;

import com.portfolio.manager.domain.*;
import com.portfolio.manager.domain.strategy_specific.PositionBookForCrown;
import com.portfolio.manager.dto.AccountDTO;
import com.portfolio.manager.dto.PortfolioDTO;
import com.portfolio.manager.dto.PositionIntegrateDTO;
import com.portfolio.manager.dto.TradeDTO;
import com.portfolio.manager.integration.OrderPlacementClient;
import com.portfolio.manager.repository.*;
import com.portfolio.manager.task.TradeTask;
import com.portfolio.manager.util.Util;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PortfolioServiceImpl implements PortfolioService {
    @Resource
    private DynamicsRepo dynamicsRepo;

    @Resource
    private PortfolioRepo portfolioRepo;

    @Resource
    private PositionRepo positionRepo;

    @Resource
    PositionBookForCrownRepo positionBookForCrownRepo;

    @Resource
    InvestorRepo investorRepo;

    @Resource
    OrderPlacementClient orderPlacemenClient;

    @Resource
    OrderService orderService;

    @Override
    public List<Position> listPosition(String portfolioName) {
        return this.getPortfolio(portfolioName).getPositions();
    }

    @Override
    public Portfolio getPortfolio(String portfolioName) {
        return portfolioRepo.findByName(portfolioName);
    }

    @Override
    public Dynamics getDynamics(Portfolio portfolio) {
        return dynamicsRepo.findByPortfolioName(portfolio.getName());
    }

    @Override
    public void updateDynamics(Dynamics dynamics) {
        dynamicsRepo.save(dynamics);
    }

    @Override
    public void updateDynamics(Set<String> codes, Portfolio portfolio) {
        //        miniQMT could response with wrong data if it is not trading day, so keep it 0 on non-trading days.
        double todayTradeTotal = Util.isTradingDay() ? orderPlacemenClient.listTodayTrades().stream().filter(trade ->
                codes.contains(trade.securityCode())
        ).mapToDouble(TradeDTO::amount).sum() : 0.0d;
        Dynamics dynamics = this.getDynamics(portfolio);
        dynamics.setCash(BigDecimal.valueOf(dynamics.getLastDayCash()).subtract(BigDecimal.valueOf(todayTradeTotal)).doubleValue());
        AccountDTO account = orderPlacemenClient.queryAcct();
        if (account == null) {
            dynamics.setSecurityMarketValue(portfolio.getPositions().stream().mapToDouble(Position::getMarketValue).sum());
        } else {
            dynamics.setSecurityMarketValue(account.marketValue().doubleValue());
        }

        dynamics.setTotalMarketValue(BigDecimal.valueOf(dynamics.getCash()).add(BigDecimal.valueOf(dynamics.getSecurityMarketValue())).doubleValue());
        this.updateDynamics(dynamics);
    }

    @Override
    public Double getCash(String portfolioName) {
        return dynamicsRepo.findByPortfolioName(portfolioName).getCash();
    }

    @Override
    public List<PortfolioDTO> listPortfolioDTO() {
        return portfolioRepo.findAll().stream().map(portfolio -> new PortfolioDTO(portfolio.getName(), portfolio.getDescription(), portfolio.getAccount())).toList();
    }

    @Override
    public List<Portfolio> listPortfolio() {
        return portfolioRepo.findAll();
    }

    @Override
    public void addPortfolio(PortfolioDTO portfolioDTO) {
        Portfolio portfolio = new Portfolio();
        portfolio.setAccount(portfolioDTO.account());
        portfolio.setName(portfolioDTO.name());
        portfolio.setDescription(portfolioDTO.description());
        portfolioRepo.save(portfolio);

        Dynamics dynamics = new Dynamics();
        dynamics.setPortfolioName(portfolio.getName());
        dynamics.setCash(1000000d);
        dynamicsRepo.save(dynamics);
    }

    @Override
    public void syncUpPositionsAndDynamicsWithBroker(Portfolio portfolio) {
        final LocalDate today = LocalDate.now();

        // 获取当前持仓的股票代码
        Set<String> codes = this.listPosition(portfolio.getName()).stream().map(Position::getSecurityCode).collect(Collectors.toSet());
        // 增加PositionBook的股票代码
        codes.addAll(positionBookForCrownRepo.findByPortfolioName(portfolio.getName()).stream().map(PositionBookForCrown::getSecurityCode).collect(Collectors.toSet()));
        // 增加order中当日的股票代码
        Set<String> securityCodesOfOrders = orderService.listOrders(portfolio.getName()).stream().parallel().filter(order -> order.getUpdateTime().toLocalDate().isEqual(today)).map(Order::getSecurityCode).collect(Collectors.toSet());
        codes.addAll(securityCodesOfOrders);

        Map<String, PositionIntegrateDTO> positionOnBrokerMap = orderPlacemenClient.queryAllPositions().stream().collect(Collectors.toMap(PositionIntegrateDTO::code, Function.identity()));
//        Update position and position book
        codes.forEach(code -> {
//            var positionOnBroker = orderPlacemenClient.checkPosition(code);
            var positionOnBroker = positionOnBrokerMap.get(code);
//            if (positionOnBroker != null && TradeTask.isOrderTime()) {
            if (TradeTask.isOrderTime()) {
                if (positionOnBroker != null && positionOnBroker.vol() != null) {
                    Optional<Position> existingPosition = portfolio.getPositions().stream().filter(p -> p.getSecurityCode().equals(code)).findFirst();
                    existingPosition.ifPresentOrElse(currentPosition -> {
//                        Update existing position
                        currentPosition.setSecurityShare(Long.valueOf(positionOnBroker.vol()));
                        currentPosition.setCost(BigDecimal.valueOf(positionOnBroker.unitCost()).multiply(BigDecimal.valueOf(currentPosition.getSecurityShare())).doubleValue());
                        currentPosition.setMarketValue(positionOnBroker.marketValue());
                        positionRepo.save(currentPosition);
                    }, () -> {
//                        Add new position
                        Position currentPosition = new Position();
                        currentPosition.setSecurityCode(code);
                        currentPosition.setSecurityShare(Long.valueOf(positionOnBroker.vol()));
                        currentPosition.setCost(BigDecimal.valueOf(positionOnBroker.unitCost()).multiply(BigDecimal.valueOf(currentPosition.getSecurityShare())).doubleValue());
                        currentPosition.setMarketValue(positionOnBroker.marketValue());
                        positionRepo.save(currentPosition);
                        portfolio.getPositions().add(currentPosition);
                    });
                } else {
                    // Remove positions
                    Optional<Position> existingPosition = portfolio.getPositions().stream().filter(p -> p.getSecurityCode().equals(code)).findFirst();
                    existingPosition.ifPresent(position -> {
                        portfolio.setPositions(portfolio.getPositions().stream().filter(p -> !p.getSecurityCode().equals(code)).toList());
                        this.updatePortfolio(portfolio);
                        this.deletePosition(position);
                        // Turn the auto mark to True if stop loss or take profit occurs
                        var positionInBook = positionBookForCrownRepo.findByPortfolioNameAndSecurityCode(portfolio.getName(), existingPosition.get().getSecurityCode());
                        positionInBook.ifPresent(positionBookForCrown -> {
                            positionBookForCrown.setSellLock(false);
                            positionBookForCrownRepo.save(positionBookForCrown);
                        });
                        // Remove from memory sell lock
                        TradeTask.sellLockSet.remove(position.getSecurityCode());
                    });
                }
            }
        });

        this.updateDynamics(codes, portfolio);
        this.updatePortfolio(portfolio);
    }

    @Override
    public void deletePosition(Position position) {
        positionRepo.delete(position);
    }

    // Static calculation
    @Override
    public List<Nav> listNavs() {
        List<Investor> investors = investorRepo.findAll();
        Map<String, BigDecimal> portfolioSharesMap = Util.getPortfolioSharesMap(investors);
        return this.listPortfolioDTO().stream().map(portfolioDTO -> {
            Portfolio portfolio = this.getPortfolio(portfolioDTO.name());
            Dynamics dynamics = this.getDynamics(portfolio);
            Nav nav = new Nav();
            nav.setPortfolioName(portfolioDTO.name());
            nav.setNav(BigDecimal.valueOf(dynamics.getTotalMarketValue()).divide(portfolioSharesMap.get(portfolioDTO.name()), 6, RoundingMode.DOWN));
            return nav;
        }).toList();
    }

    @Override
    public void updatePortfolio(Portfolio portfolio) {
        portfolioRepo.save(portfolio);
    }


}
