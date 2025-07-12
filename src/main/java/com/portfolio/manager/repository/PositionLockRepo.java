package com.portfolio.manager.repository;

import com.portfolio.manager.domain.strategy_specific.PositionLock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PositionLockRepo extends JpaRepository<PositionLock, Long> {
    PositionLock findBySecurityCode(String code);
}
