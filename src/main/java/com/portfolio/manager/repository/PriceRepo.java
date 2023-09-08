package com.portfolio.manager.repository;

import com.portfolio.manager.domain.Price;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PriceRepo extends JpaRepository<Price, Long> {
    List<Price> findAllByCode(String code);
}
