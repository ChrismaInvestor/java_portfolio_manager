package com.portfolio.manager.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Setter
@RequiredArgsConstructor
@Entity(name = "trade")
@EntityListeners(AuditingEntityListener.class)
public class Trade {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createTime;

    @LastModifiedDate
    private LocalDateTime updateTime;

    private Long orderId;

//    Added on 2024-02-26
    private Long subOrderId;

    private Long clientOrderId;

    @NotNull
    @Pattern(regexp = "[0-9]{6}")
    @Column(length = 6)
    private String code;

    @NotNull
    private Double price;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Direction direction;

    @NotNull
    private Long volume;

}
