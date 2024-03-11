package com.portfolio.manager.domain;

import com.portfolio.manager.domain.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@Setter
@RequiredArgsConstructor
@Entity(name = "nav")
@ToString(callSuper = true)
public class Nav extends BaseEntity {
    @Column(precision = 30, scale = 6)
    private BigDecimal nav;
    private String portfolioName;
}
