package com.portfolio.manager.domain;

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
@Entity(name = "dynamics")
@ToString(callSuper = true)
public class Dynamics extends BaseEntity {
    @Column(unique = true)
    private String portfolioName;
    private Double cash;
    private Double lastDayCash;
    private Double totalMarketValue;
    private Double securityMarketValue;
    private Double profitMargin;
}
