package com.portfolio.manager.repository;


import com.portfolio.manager.domain.strategy_specific.PositionBookForCrown;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface PositionBookForCrownRepo extends JpaRepository<PositionBookForCrown, Long> {

    List<PositionBookForCrown> findByPortfolioName(String portfolioName);

    @Transactional
    void deleteByPortfolioName(String portfolioName);
}
