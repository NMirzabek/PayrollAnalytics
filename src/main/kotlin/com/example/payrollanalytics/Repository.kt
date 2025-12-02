package com.example.payrollanalytics

import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

interface EmployeeRepository : JpaRepository<Employee, Long> {
    fun findByPinfl(pinfl: String): Employee?
}

interface OrganizationRepository : JpaRepository<Organization, Long> {
    fun findAllByParent_Id(parentId: Long): List<Organization>
}

interface RegionRepository : JpaRepository<Region, Long>

interface CalculationTableRepository : JpaRepository<CalculationTable, Long> {

    fun findAllByDateBetween(start: LocalDate, end: LocalDate): List<CalculationTable>

    fun findAllByDateBetweenAndCalculationType(
        start: LocalDate,
        end: LocalDate,
        calculationType: CalculationType
    ): List<CalculationTable>
}