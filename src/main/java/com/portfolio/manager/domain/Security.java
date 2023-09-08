package com.portfolio.manager.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@RequiredArgsConstructor
@Entity(name = "security")
@ToString(callSuper = true)
public class Security extends BaseEntity{
    @Column(unique = true)
    private String code;
    private String name;
}
