package com.portfolio.manager.domain;

import com.portfolio.manager.domain.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@RequiredArgsConstructor
@Entity(name = "cov_info")
@ToString(callSuper = true)
public class CovInfo extends BaseEntity {
    @Column(unique = true, length = 6)
    private String code;

    @Column(length = 6)
    private String stockCode;
    @NotNull
    private Double convertedPrice;
}
