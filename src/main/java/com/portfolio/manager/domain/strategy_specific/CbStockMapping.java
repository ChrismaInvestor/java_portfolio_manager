package com.portfolio.manager.domain.strategy_specific;

import com.portfolio.manager.domain.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@RequiredArgsConstructor
@ToString(callSuper = true)
@Entity
public class CbStockMapping extends BaseEntity {
    @Setter
    @Column(length = 6, unique = true)
    private String cbCode;

    @Setter
    @Column(length = 6)
    private String stockCode;
}
