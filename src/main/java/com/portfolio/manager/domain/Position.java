package com.portfolio.manager.domain;

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
public class Position extends BaseEntity{
    private String securityCode;

    private Double securityAmount;
}
