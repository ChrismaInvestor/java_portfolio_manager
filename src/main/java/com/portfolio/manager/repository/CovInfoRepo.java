package com.portfolio.manager.repository;

import com.portfolio.manager.domain.CovInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CovInfoRepo extends JpaRepository<CovInfo, Long> {
    CovInfo findByCode(String code);
}
