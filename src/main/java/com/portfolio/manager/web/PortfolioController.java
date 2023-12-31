package com.portfolio.manager.web;

import com.portfolio.manager.domain.Dynamics;
import com.portfolio.manager.domain.Portfolio;
import com.portfolio.manager.dto.PortfolioDTO;
import com.portfolio.manager.repository.DynamicsRepo;
import com.portfolio.manager.repository.PortfolioRepo;
import com.portfolio.manager.service.PortfolioService;
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
    DynamicsRepo dynamicsRepo;

    @Resource
    PortfolioService portfolioService;

    @Resource
    PortfolioRepo portfolioRepo;

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

    @PostMapping
    public void addPortfolio(@RequestBody PortfolioDTO portfolioDTO) {
        portfolioService.addPortfolio(portfolioDTO);
    }
}
