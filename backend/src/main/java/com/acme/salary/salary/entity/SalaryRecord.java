package com.acme.salary.salary.entity;

import com.acme.salary.auth.entity.UserAccount;
import com.acme.salary.employee.entity.Employee;
import com.acme.salary.organization.entity.Currency;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "salary_records")
public class SalaryRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "currency_code", nullable = false)
    private Currency currency;

    @Column(name = "pay_frequency", nullable = false, length = 20)
    private String payFrequency = "ANNUAL";

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id")
    private UserAccount createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Version
    @Column(nullable = false)
    private Long version = 0L;

    public SalaryRecord() {}

    public SalaryRecord(Employee employee, BigDecimal amount, Currency currency,
                        String payFrequency, LocalDate effectiveFrom, LocalDate effectiveTo,
                        UserAccount createdBy) {
        this.employee = employee;
        this.amount = amount;
        this.currency = currency;
        this.payFrequency = payFrequency;
        this.effectiveFrom = effectiveFrom;
        this.effectiveTo = effectiveTo;
        this.createdBy = createdBy;
    }

    public boolean isActive() {
        return this.effectiveTo == null;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public String getPayFrequency() {
        return payFrequency;
    }

    public void setPayFrequency(String payFrequency) {
        this.payFrequency = payFrequency;
    }

    public LocalDate getEffectiveFrom() {
        return effectiveFrom;
    }

    public void setEffectiveFrom(LocalDate effectiveFrom) {
        this.effectiveFrom = effectiveFrom;
    }

    public LocalDate getEffectiveTo() {
        return effectiveTo;
    }

    public void setEffectiveTo(LocalDate effectiveTo) {
        this.effectiveTo = effectiveTo;
    }

    public UserAccount getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(UserAccount createdBy) {
        this.createdBy = createdBy;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public Long getVersion() {
        return version;
    }
}
