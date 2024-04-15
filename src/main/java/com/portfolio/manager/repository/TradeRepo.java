package com.portfolio.manager.repository;

import com.portfolio.manager.domain.Trade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TradeRepo extends JpaRepository<Trade, Long> {
    List<Trade> findByClientOrderIdOrderByCreateTimeDesc(Long clientOrderId);
}
