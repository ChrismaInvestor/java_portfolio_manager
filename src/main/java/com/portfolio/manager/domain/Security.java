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
@Entity(name = "security")
@ToString(callSuper = true)
public class Security extends BaseEntity {
    @Column(unique = true, length = 6)
    private String code;
    @Column(unique = true, length = 20)
    private String name;
}
