package com.portfolio.manager.web;

import com.portfolio.manager.domain.Dynamics;
import com.portfolio.manager.domain.Portfolio;
import com.portfolio.manager.dto.PortfolioDTO;
import com.portfolio.manager.repository.DynamicsRepo;
import com.portfolio.manager.repository.PortfolioRepo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/portfolio")
@CrossOrigin
public class PortfolioController {
    @Resource
    PortfolioRepo portfolioRepo;

    @Resource
    DynamicsRepo dynamicsRepo;

    @GetMapping
    public List<PortfolioDTO> listPortfolio() {
        return portfolioRepo.findAll().stream().map(portfolio -> new PortfolioDTO(portfolio.getName(), portfolio.getDescription(), portfolio.getAccount())).toList();
    }

    @PostMapping
    public void addPortfolio(@RequestBody PortfolioDTO portfolioDTO) {
        Portfolio portfolio = new Portfolio();
        portfolio.setAccount(portfolioDTO.account());
        portfolio.setName(portfolioDTO.name());
        portfolio.setDescription(portfolioDTO.description());
        log.info("portfolio: {}", portfolio);
        portfolioRepo.save(portfolio);

        Dynamics dynamics = new Dynamics();
        dynamics.setPortfolioName(portfolio.getName());
        dynamics.setCash(1000000d);
        dynamicsRepo.save(dynamics);
    }

}
