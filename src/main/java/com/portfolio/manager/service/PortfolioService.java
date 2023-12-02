package com.portfolio.manager.service;

import com.portfolio.manager.domain.Position;
import com.portfolio.manager.dto.PortfolioDTO;

import java.util.List;

public interface PortfolioService {
    List<Position> listPosition(String portfolioName);

    Double getCash(String portfolioName);

    List<PortfolioDTO> listPortfolio();

    void addPortfolio(PortfolioDTO portfolioDTO);

    void appendPositions(PortfolioDTO portfolioDTO, List<Position> positions);

}
