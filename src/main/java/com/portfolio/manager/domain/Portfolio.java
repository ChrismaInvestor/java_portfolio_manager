package com.portfolio.manager.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.*;
import org.hibernate.Hibernate;

import java.util.Objects;

@Getter
@Setter
@RequiredArgsConstructor
@Entity(name = "portfolio")
@ToString(callSuper = true)
public class Portfolio extends BaseEntity{

    @Column(unique = true)
    private String name;
    private String description;
    private String account;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Portfolio portfolio = (Portfolio) o;
        return getId() != null && Objects.equals(getId(), portfolio.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
