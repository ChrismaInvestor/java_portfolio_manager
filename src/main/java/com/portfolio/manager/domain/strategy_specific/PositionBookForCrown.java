package com.portfolio.manager.domain.strategy_specific;

import com.portfolio.manager.domain.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;


@RequiredArgsConstructor
@ToString(callSuper = true)
@Entity
public class PositionBookForCrown extends BaseEntity {
    @Getter
    @Setter
    @Column(length = 6)
    private String securityCode;

    @Getter
    @Setter
    private Long securityShare;

    @Getter
    @Setter
    private String securityName;

    @Getter
    @Setter
    private String portfolioName;

    @Getter
    @Setter
    private Boolean sellLock;

    @Getter
    private Boolean buyBack;
}
