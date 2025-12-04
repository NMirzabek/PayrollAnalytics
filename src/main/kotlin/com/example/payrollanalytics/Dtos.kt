package com.example.payrollanalytics

import java.math.BigDecimal
import java.time.LocalDate

data class EmployeeCreateRequest(
    val firstName: String,
    val lastName: String,
    val pinfl: String,
    val hireDate: LocalDate,
    val organizationId: Long
)

data class EmployeeResponse(
    val id: Long,
    val firstName: String,
    val lastName: String,
    val pinfl: String,
    val hireDate: LocalDate,
    val organizationId: Long
)

data class OrganizationCreateRequest(
    val name: String,
    val regionId: Long,
    val parentId: Long?
)

data class OrganizationResponse(
    val id: Long,
    val name: String,
    val regionId: Long,
    val parentId: Long?
)

data class RegionCreateRequest(
    val name: String
)

data class RegionResponse(
    val id: Long,
    val name: String
)

data class CalculationCreateRequest(
    val employeeId: Long,
    val amount: BigDecimal,
    val rate: BigDecimal,
    val date: LocalDate,
    val organizationId: Long,
    val calculationType: CalculationType
)

data class CalculationResponse(
    val id: Long,
    val employeeId: Long,
    val amount: BigDecimal,
    val rate: BigDecimal,
    val date: LocalDate,
    val organizationId: Long,
    val calculationType: CalculationType
)

data class OverRateEmployeeDto(
    val pinfl: String,
    val totalRate: BigDecimal
)


data class MultiRegionEmployeeDto(
    val pinfl: String,
    val organizationCount: Long,
    val totalSalary: BigDecimal
)

data class OrganizationAverageSalaryDto(
    val organizationId: Long,
    val organizationName: String,
    val employeeId: Long,
    val employeeFullName: String,
    val averageSalary: Double
)

data class SalaryAndVacationDto(
    val employeeId: Long,
    val fullName: String,
    val pinfl: String,
    val salaryAmount: BigDecimal,
    val vacationAmount: BigDecimal
)