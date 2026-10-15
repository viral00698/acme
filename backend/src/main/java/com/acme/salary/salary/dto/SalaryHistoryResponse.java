package com.acme.salary.salary.dto;

import java.util.List;

public record SalaryHistoryResponse(
        Long employeeId,
        String employeeCode,
        String employeeName,
        SalaryRecordDto currentSalary,
        List<SalaryRecordDto> history
) {}
