package com.portfolio.manager.service;

import com.portfolio.manager.domain.Dynamics;
import com.portfolio.manager.domain.Portfolio;
import com.portfolio.manager.domain.Position;
import com.portfolio.manager.domain.strategy_specific.PositionBookForCrown;
import com.portfolio.manager.dto.PortfolioDTO;
import com.portfolio.manager.dto.PositionIntegrateDTO;
import com.portfolio.manager.dto.TradeDTO;
import com.portfolio.manager.integration.OrderPlacementService;
import com.portfolio.manager.repository.DynamicsRepo;
import com.portfolio.manager.repository.PortfolioRepo;
import com.portfolio.manager.repository.PositionBookForCrownRepo;
import com.portfolio.manager.repository.PositionRepo;
import com.portfolio.manager.task.TradeTask;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
    OrderPlacementService orderPlacementService;

    @Override
    public List<Position> listPosition(String portfolioName) {
        return portfolioRepo.findByName(portfolioName).getPositions();
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
    public void updateDynamics(Double todayTradeTotal, Portfolio portfolio) {
        Dynamics dynamics = this.getDynamics(portfolio);
        dynamics.setCash(BigDecimal.valueOf(dynamics.getLastDayCash()).subtract(BigDecimal.valueOf(todayTradeTotal)).doubleValue());
        dynamics.setSecurityMarketValue(portfolio.getPositions().stream().mapToDouble(Position::getMarketValue).sum());
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
    public void updatePosition(Position position) {
        positionRepo.save(position);
    }

    @Override
    public void syncUpPositions(Portfolio portfolio) {
        Set<String> codes = this.listPosition(portfolio.getName()).stream().map(Position::getSecurityCode).collect(Collectors.toSet());
        codes.addAll(positionBookForCrownRepo.findByPortfolioName(portfolio.getName()).stream().map(PositionBookForCrown::getSecurityCode).collect(Collectors.toSet()));

        codes.forEach(code -> {
            PositionIntegrateDTO positionOnBroker = orderPlacementService.checkPosition(code);
            if (positionOnBroker != null && TradeTask.isOrderTime()) {
                if (positionOnBroker.vol() != null) {
                    Optional<Position> existingPosition = portfolio.getPositions().stream().filter(p -> p.getSecurityCode().equals(code)).findFirst();
                    if (existingPosition.isEmpty()) {
                        // Add position
                        Position currentPosition = new Position();
                        currentPosition.setSecurityCode(code);
                        currentPosition.setSecurityShare(Long.valueOf(positionOnBroker.vol()));
                        currentPosition.setCost(BigDecimal.valueOf(positionOnBroker.unitCost()).multiply(BigDecimal.valueOf(currentPosition.getSecurityShare())).doubleValue());
                        currentPosition.setMarketValue(positionOnBroker.marketValue());
                        this.updatePosition(currentPosition);
                        portfolio.getPositions().add(currentPosition);
                    } else {
                        // Update position
                        Position currentPosition = existingPosition.get();
                        currentPosition.setSecurityShare(Long.valueOf(positionOnBroker.vol()));
                        currentPosition.setCost(BigDecimal.valueOf(positionOnBroker.unitCost()).multiply(BigDecimal.valueOf(currentPosition.getSecurityShare())).doubleValue());
                        currentPosition.setMarketValue(positionOnBroker.marketValue());
                        this.updatePosition(currentPosition);
                    }
                } else {
                    // Remove positions
                    Optional<Position> existingPosition = portfolio.getPositions().stream().filter(p -> p.getSecurityCode().equals(code)).findFirst();
                    if (existingPosition.isPresent()) {
                        portfolio.setPositions(portfolio.getPositions().stream().filter(p -> !p.getSecurityCode().equals(code)).toList());
                        this.updatePortfolio(portfolio);
                        this.deletePosition(existingPosition.get());
                        // Turn the auto mark to True if stop loss or take profit occurs
                        Optional<PositionBookForCrown> book = positionBookForCrownRepo.findByPortfolioNameAndSecurityCode(portfolio.getName(), existingPosition.get().getSecurityCode());
                        book.ifPresent(positionBookForCrown -> {
                            positionBookForCrown.setSellLock(false);
//                            positionBookForCrown.setBuyBack(false);
                            positionBookForCrownRepo.save(positionBookForCrown);
                        });
                    }
                }
            }
        });

        //Update dynamics
        double todayTradeTotal = orderPlacementService.listTodayTrades().stream().filter(trade ->
                codes.contains(trade.securityCode())
        ).mapToDouble(TradeDTO::amount).sum();
        this.updateDynamics(todayTradeTotal, portfolio);
        this.updatePortfolio(portfolio);
    }

    @Override
    public void deletePosition(Position position) {
        positionRepo.delete(position);
    }

    @Override
    public void updatePortfolio(Portfolio portfolio) {
        portfolioRepo.save(portfolio);
    }


}
