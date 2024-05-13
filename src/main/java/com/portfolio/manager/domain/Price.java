package com.portfolio.manager.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Setter
@RequiredArgsConstructor
public class Price {

    @NotNull
    @Pattern(regexp = "[0-9]{6}")
    @Column(length = 6)
    private String code;
    @NotNull
    private Double amount;

    @NotNull
    private Long volume;

    private Long time;
}
