package com.portfolio.manager.domain;

import com.portfolio.manager.domain.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@RequiredArgsConstructor
@Entity(name = "suborders")
@ToString(callSuper = true)
public class SubOrder extends BaseEntity {

    private Long plannedShare;
    private Long remainingShare;

    LocalDateTime startTime;
    LocalDateTime endTime;
    private String buyOrSell;
    @Column(length = 6)
    private String securityCode;
}
