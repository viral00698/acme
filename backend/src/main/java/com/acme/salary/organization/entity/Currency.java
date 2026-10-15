package com.acme.salary.organization.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "currencies")
public class Currency {

    @Id
    @Column(length = 3)
    private String code;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 5)
    private String symbol;

    public Currency() {}

    public Currency(String code, String name, String symbol) {
        this.code = code;
        this.name = name;
        this.symbol = symbol;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
}
