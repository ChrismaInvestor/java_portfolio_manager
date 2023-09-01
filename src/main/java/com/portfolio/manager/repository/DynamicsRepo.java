package com.portfolio.manager.repository;

import com.portfolio.manager.domain.Dynamics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DynamicsRepo extends JpaRepository<Dynamics, Long> {
    Dynamics findByPortfolioName(String portfolioName);
}
