package com.portfolio.manager.service;

import com.portfolio.manager.domain.Portfolio;
import com.portfolio.manager.domain.Position;
import com.portfolio.manager.dto.PortfolioDTO;

import java.util.List;

public interface PortfolioService {
    List<Position> listPosition(String portfolioName);

    Portfolio getPortfolio(String portfolioName);

    Double getCash(String portfolioName);

    List<PortfolioDTO> listPortfolio();

    void addPortfolio(PortfolioDTO portfolioDTO);

    void appendPositions(PortfolioDTO portfolioDTO, List<Position> positions);

    void updatePosition(Position position);

    void deletePosition(Position position);

    void updatePortfolio(Portfolio portfolio);
}
