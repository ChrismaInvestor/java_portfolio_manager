package com.portfolio.manager.repository;

import com.portfolio.manager.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepo extends JpaRepository<Order, Long> {
}
