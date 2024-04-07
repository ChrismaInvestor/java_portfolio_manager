package com.portfolio.manager.repository;

import com.portfolio.manager.domain.PositionSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PositionSnapshotRepo extends JpaRepository<PositionSnapshot, Long> {
}
