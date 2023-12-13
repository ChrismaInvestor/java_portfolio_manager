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
@Entity(name = "position")
@ToString(callSuper = true)
public class Position extends BaseEntity {
    @Column(length = 6)
    private String securityCode;

    private Long securityShare;

    private Double cost;

    private Double marketValue;
}
