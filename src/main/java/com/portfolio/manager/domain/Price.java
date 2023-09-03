package com.portfolio.manager.domain;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@RequiredArgsConstructor
@Entity(name = "price")
@ToString(callSuper = true)
public class Price extends BaseEntity{
    private String code;
    private Double price;
    private Long volume;

    private LocalDateTime time;
}
