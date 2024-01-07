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
public class PositionBookForCrown extends BaseEntity {
    @Setter
    @Column(length = 6)
    private String securityCode;

    @Setter
    private Long securityShare;

    @Setter
    private String securityName;

    @Setter
    private String portfolioName;

    @Setter
    private Boolean sellLock;

    private Boolean buyBack;
}
