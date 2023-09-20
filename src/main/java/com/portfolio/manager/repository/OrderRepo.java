package com.portfolio.manager.repository;

import com.portfolio.manager.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepo extends JpaRepository<Order, Long> {
    List<Order> findByPortfolioName(String portfolioName);
}
