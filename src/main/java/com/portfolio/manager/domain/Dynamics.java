package com.portfolio.manager.domain;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@RequiredArgsConstructor
@Entity(name = "dynamics")
@ToString(callSuper = true)
public class Dynamics extends BaseEntity{
    @Column(unique = true)
    private String portfolioName;
    private Double cash;
    private Double totalMarketValue;
    private Double securityMarketValue;
    private Double profitMargin;

    @OneToMany
    @ToString.Exclude
    private List<Position> positions;
}
