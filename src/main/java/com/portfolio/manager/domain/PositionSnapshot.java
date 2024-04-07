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
@Entity(name = "position_snapshot")
@ToString(callSuper = true)
public class PositionSnapshot extends BaseEntity {
    @Column(length = 6)
    private String securityCode;

    private Long securityShare;
}
