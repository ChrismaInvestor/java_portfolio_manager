package com.portfolio.manager.repository;


import com.portfolio.manager.domain.strategy_specific.PositionBookForCrown;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface PositionBookForCrownRepo extends JpaRepository<PositionBookForCrown, Long> {

    List<PositionBookForCrown> findByPortfolioName(String portfolioName);

    Optional<PositionBookForCrown> findByPortfolioNameAndSecurityCode(String portfolioName, String securityCode);

    @Transactional
    void deleteByPortfolioName(String portfolioName);
}
