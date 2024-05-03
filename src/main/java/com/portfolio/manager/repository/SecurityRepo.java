package com.portfolio.manager.repository;

import com.portfolio.manager.domain.Security;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface SecurityRepo extends JpaRepository<Security, Long> {
    Security findOneByCode(String code);

    @Transactional
    void deleteAll();
}
