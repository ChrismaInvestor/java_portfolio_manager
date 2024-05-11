package com.portfolio.manager.web;

import com.portfolio.manager.domain.Dynamics;
import com.portfolio.manager.domain.Investor;
import com.portfolio.manager.domain.Nav;
import com.portfolio.manager.domain.Portfolio;
import com.portfolio.manager.dto.InvestorPLDTO;
import com.portfolio.manager.dto.NavDTO;
import com.portfolio.manager.dto.PortfolioDTO;
import com.portfolio.manager.repository.DynamicsRepo;
import com.portfolio.manager.repository.InvestorRepo;
import com.portfolio.manager.repository.NavRepo;
import com.portfolio.manager.repository.PortfolioRepo;
import com.portfolio.manager.service.PortfolioService;
import com.portfolio.manager.util.Util;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
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

    @Resource
    NavRepo navRepo;

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

    @GetMapping("nav")
    public List<NavDTO> listNav(){
        List<Nav> ans = new ArrayList<>();
        portfolioRepo.findAll().forEach(portfolio -> ans.addAll(navRepo.findByPortfolioName(portfolio.getName())));
        return ans.stream().filter(nav -> nav.getCreateTime().toLocalDate().isAfter(LocalDate.of(2024,4,15))).map(nav -> new NavDTO(nav.getCreateTime().toLocalDate(), nav.getNav(), nav.getPortfolioName())).toList();
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
