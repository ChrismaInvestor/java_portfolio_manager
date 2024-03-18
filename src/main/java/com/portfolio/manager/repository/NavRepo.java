package com.portfolio.manager.repository;

import com.portfolio.manager.domain.Nav;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NavRepo extends JpaRepository<Nav,Long> {
    List<Nav> findByPortfolioName(String portfolioName);
}
