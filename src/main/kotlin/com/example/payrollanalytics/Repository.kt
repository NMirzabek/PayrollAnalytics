package com.example.payrollanalytics

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.math.BigDecimal
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

    @Query(
        """
        select new com.example.payrollanalytics.OverRateEmployeeDto(
            e.pinfl,
            sum(c.rate)
        )
        from CalculationTable c
        join c.employee e
        where c.date >= :start
          and c.date < :end
        group by e.pinfl
        having sum(c.rate) > :minRate
        """
    )
    fun findEmployeesWithTotalRateGreaterThan(
        @Param("start") start: LocalDate,
        @Param("end") end: LocalDate,
        @Param("minRate") minRate: BigDecimal
    ): List<OverRateEmployeeDto>

    @Query(
        """
        select new com.example.payrollanalytics.MultiRegionEmployeeDto(
            e.pinfl,
            count(distinct o.id),
            sum(c.amount)
        )
        from CalculationTable c
        join c.employee e
        join c.organization o
        join o.region r
        where c.date >= :start
          and c.date < :end
          and c.calculationType = com.example.payrollanalytics.CalculationType.SALARY
        group by e.pinfl
        having count(distinct r.id) > 1
        """
    )
    fun findEmployeesWorkedInDifferentRegions(
        @Param("start") start: LocalDate,
        @Param("end") end: LocalDate
    ): List<MultiRegionEmployeeDto>

    @Query(
        """
        select new com.example.payrollanalytics.OrganizationAverageSalaryDto(
            o.id,
            o.name,
            e.id,
            concat(e.firstName, ' ', e.lastName),
            avg(c.amount)
        )
        from CalculationTable c
        join c.employee e
        join c.organization o
        where c.date >= :start
          and c.date < :end
          and c.calculationType = com.example.payrollanalytics.CalculationType.SALARY
          and (o.id = :orgId or o.parent.id = :orgId)
        group by o.id, o.name, e.id, e.firstName, e.lastName
        """
    )
    fun findOrganizationAverageSalariesForOrgAndChildren(
        @Param("start") start: LocalDate,
        @Param("end") end: LocalDate,
        @Param("orgId") orgId: Long
    ): List<OrganizationAverageSalaryDto>

    @Query(
        """
        select new com.example.payrollanalytics.SalaryAndVacationDto(
            e.id,
            concat(e.firstName, ' ', e.lastName),
            e.pinfl,
            sum(case when c.calculationType = com.example.payrollanalytics.CalculationType.SALARY then c.amount else 0 end),
            sum(case when c.calculationType = com.example.payrollanalytics.CalculationType.VACATION then c.amount else 0 end)
        )
        from CalculationTable c
        join c.employee e
        where c.date >= :start
          and c.date < :end
        group by e.id, e.firstName, e.lastName, e.pinfl
        having sum(case when c.calculationType = com.example.payrollanalytics.CalculationType.SALARY then c.amount else 0 end) > 0
           and sum(case when c.calculationType = com.example.payrollanalytics.CalculationType.VACATION then c.amount else 0 end) > 0
        """
    )
    fun findEmployeesWithSalaryAndVacation(
        @Param("start") start: LocalDate,
        @Param("end") end: LocalDate
    ): List<SalaryAndVacationDto>
}
