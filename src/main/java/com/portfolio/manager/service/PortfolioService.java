package com.portfolio.manager.service;

import com.portfolio.manager.domain.Dynamics;
import com.portfolio.manager.domain.Portfolio;
import com.portfolio.manager.domain.Position;
import com.portfolio.manager.dto.PortfolioDTO;

import java.util.List;
import java.util.Set;

public interface PortfolioService {
    List<Position> listPosition(String portfolioName);

    Portfolio getPortfolio(String portfolioName);

    Dynamics getDynamics(Portfolio portfolio);

    void updateDynamics(Dynamics dynamics);

    void updateDynamics(Double todayTradeTotal, Portfolio portfolio);

    Double getCash(String portfolioName);

    List<PortfolioDTO> listPortfolioDTO();
    List<Portfolio> listPortfolio();

    void addPortfolio(PortfolioDTO portfolioDTO);

    void updatePortfolio(Portfolio portfolio);

    void updatePosition(Position position);

    void syncUpPositionsAndDynamics(Portfolio portfolio, Set<String> codesOfOrders);

    void deletePosition(Position position);
}
