package com.portfolio.manager.domain;

import com.portfolio.manager.domain.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@RequiredArgsConstructor
@Entity(name = "orders")
@ToString(callSuper = true)
public class Order extends BaseEntity {
    @Enumerated(EnumType.STRING)
    private Direction buyOrSell;
    private Long plannedShare;
    private Long remainingShare;
    private String portfolioName;

    @Column(length = 6)
    private String securityCode;

    @OneToMany(fetch = FetchType.EAGER)
    private List<SubOrder> subOrders;
}
