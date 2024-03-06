package com.portfolio.manager.repository;

import com.portfolio.manager.domain.Nav;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NavRepo extends JpaRepository<Nav,Long> {
}
