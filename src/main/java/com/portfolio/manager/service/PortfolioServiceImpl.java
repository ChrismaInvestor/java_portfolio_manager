package com.portfolio.manager.service;

import com.portfolio.manager.domain.Dynamics;
import com.portfolio.manager.domain.Portfolio;
import com.portfolio.manager.domain.Position;
import com.portfolio.manager.dto.PortfolioDTO;
import com.portfolio.manager.repository.DynamicsRepo;
import com.portfolio.manager.repository.PortfolioRepo;
import com.portfolio.manager.repository.PositionRepo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
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

    @Override
    public List<Position> listPosition(String portfolioName) {
        return portfolioRepo.findByName(portfolioName).getPositions();
    }

    @Override
    public Portfolio getPortfolio(String portfolioName) {
        return portfolioRepo.findByName(portfolioName);
    }

    @Override
    public Double getCash(String portfolioName) {
        return dynamicsRepo.findByPortfolioName(portfolioName).getCash();
    }

    @Override
    public List<PortfolioDTO> listPortfolio() {
        return portfolioRepo.findAll().stream().map(portfolio -> new PortfolioDTO(portfolio.getName(), portfolio.getDescription(), portfolio.getAccount())).toList();
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
    public void appendPositions(PortfolioDTO portfolioDTO, List<Position> positions) {
        Portfolio portfolio = portfolioRepo.findByName(portfolioDTO.name());
        if (!portfolio.getPositions().isEmpty()) {
            Map<String, Position> map = positions.stream().collect(Collectors.toMap(Position::getSecurityCode, Function.identity()));
            portfolio.getPositions().forEach(position -> {
                if (map.get(position.getSecurityCode()) != null) {
                    position.setCost(map.get(position.getSecurityCode()).getCost());
                    position.setSecurityShare(map.get(position.getSecurityCode()).getSecurityShare());
                    positionRepo.save(position);
                }
            });
        } else {
            positionRepo.saveAll(positions);
            portfolio.setPositions(positions);
        }
        portfolioRepo.save(portfolio);
    }

    @Override
    public void updatePosition(Position position) {
        positionRepo.save(position);
    }

    @Override
    public void updatePortfolio(Portfolio portfolio) {
        portfolioRepo.save(portfolio);
    }


}
