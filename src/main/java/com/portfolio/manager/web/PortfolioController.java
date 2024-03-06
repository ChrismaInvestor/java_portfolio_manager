package com.portfolio.manager.web;

import com.portfolio.manager.domain.Dynamics;
import com.portfolio.manager.domain.Investor;
import com.portfolio.manager.domain.Portfolio;
import com.portfolio.manager.dto.InvestorPLDTO;
import com.portfolio.manager.dto.PortfolioDTO;
import com.portfolio.manager.repository.DynamicsRepo;
import com.portfolio.manager.repository.InvestorRepo;
import com.portfolio.manager.repository.PortfolioRepo;
import com.portfolio.manager.service.PortfolioService;
import com.portfolio.manager.util.Util;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/portfolio")
@CrossOrigin
public class PortfolioController {
    @Resource
    DynamicsRepo dynamicsRepo;

    @Resource
    PortfolioService portfolioService;

    @Resource
    PortfolioRepo portfolioRepo;

    @Resource
    InvestorRepo investorRepo;

    @GetMapping
    public List<PortfolioDTO> listPortfolio() {
        return portfolioService.listPortfolioDTO();
    }

    @GetMapping("dynamics")
    public Dynamics getDynamics(@RequestParam(name = "currentPortfolio") String portfolio) {
        return dynamicsRepo.findByPortfolioName(portfolio);
    }

    @GetMapping("portfolio")
    public Portfolio getPortfolio(@RequestParam(name = "currentPortfolio") String portfolio) {
        return portfolioRepo.findByName(portfolio);
    }

    @GetMapping("investorBook")
    public List<InvestorPLDTO> listInvestorBook() {
        List<Investor> investors = investorRepo.findAll();
        Map<String, BigDecimal> portfolioSharesMap = Util.getPortfolioSharesMap(investors);
        return investors.stream().map(investor -> {
            Dynamics dynamics = dynamicsRepo.findByPortfolioName(investor.getPortfolioName());
            BigDecimal nav = BigDecimal.valueOf(dynamics.getTotalMarketValue()).divide(portfolioSharesMap.get(investor.getPortfolioName()),6, RoundingMode.DOWN);
            return new InvestorPLDTO(investor.getName(), investor.getShareAmount().toString(), nav.toString(), investor.getPortfolioName());
        }).toList();
    }

    @PostMapping
    public void addPortfolio(@RequestBody PortfolioDTO portfolioDTO) {
        portfolioService.addPortfolio(portfolioDTO);
    }
}
