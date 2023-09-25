package com.portfolio.manager.repository;

import com.portfolio.manager.domain.SubOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubOrderRepo extends JpaRepository<SubOrder, Long> {
}
