package com.portfolio.manager.repository;

import com.portfolio.manager.domain.strategy_specific.CbStockMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CbStockMappingRepo extends JpaRepository<CbStockMapping, Long> {
    Optional<CbStockMapping> findByCbCode(String cbCode);
}
