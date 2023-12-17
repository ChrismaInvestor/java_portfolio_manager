package com.portfolio.manager.domain.strategy_specific;

import com.portfolio.manager.domain.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@RequiredArgsConstructor
@ToString(callSuper = true)
@Entity
public class PositionBookForCrown extends BaseEntity {
    @Column(length = 6)
    private String securityCode;

    private Long securityShare;

    private String securityName;

    private String portfolioName;
}
