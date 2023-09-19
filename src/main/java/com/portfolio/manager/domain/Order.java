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
@Entity(name = "orders")
@ToString(callSuper = true)
public class Order extends BaseEntity {
    private String buyOrSell;
    private Long plannedShare;
    private Long remainingShare;
    private String portfolioName;

    @Column(length = 6)
    private String securityCode;
}
