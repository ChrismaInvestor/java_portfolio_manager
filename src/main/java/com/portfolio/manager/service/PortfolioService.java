package com.portfolio.manager.service;

import com.portfolio.manager.domain.Position;
import com.portfolio.manager.dto.OrderDTO;
import com.portfolio.manager.dto.PortfolioDTO;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Set;

public interface PortfolioService {
    List<Position> listPosition(String portfolioName);

    Double getCash(String portfolioName);

    List<PortfolioDTO> listPortfolio();

    void addPortfolio(PortfolioDTO portfolioDTO);

//    //Estimation
//    List<OrderDTO> buySplitEven(Set<String> securityCodes, double toSellMarketValue, double cash, List<Position> holdings);
//
//    //Estimation
//    List<OrderDTO> sell(List<Position> toSell);
}
