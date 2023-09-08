package com.portfolio.manager.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.text.DecimalFormat;
import java.time.LocalDateTime;

@Getter
@Setter
@RequiredArgsConstructor
@Entity(name = "price")
@ToString(callSuper = true)
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"code","time"})})
public class Price extends BaseEntity{
    @NotNull
    @Pattern(regexp = "[0-9]{6}")
    @Column(length = 6)
    private String code;
    @NotNull
    private Double price;

    @NotNull
    private Long volume;

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime time;
}
