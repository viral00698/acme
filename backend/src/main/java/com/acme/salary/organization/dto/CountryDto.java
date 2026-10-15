package com.acme.salary.organization.dto;

public record CountryDto(
        Long id,
        String code,
        String name,
        String defaultCurrencyCode
) {}
