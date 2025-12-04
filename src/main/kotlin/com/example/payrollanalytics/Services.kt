package com.example.payrollanalytics

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.RoundingMode

@Service
class EmployeeService(
    private val employeeRepository: EmployeeRepository,
    private val organizationRepository: OrganizationRepository
) {
    fun create(req: EmployeeCreateRequest): EmployeeResponse {
        val org = organizationRepository.findById(req.organizationId)
            .orElseThrow { IllegalArgumentException("Organization not found") }

        val saved = employeeRepository.save(
            Employee(
                firstName = req.firstName,
                lastName = req.lastName,
                pinfl = req.pinfl,
                hireDate = req.hireDate,
                organization = org,
            )
        )

        return EmployeeResponse(
            id = saved.id!!,
            firstName = saved.firstName,
            lastName = saved.lastName,
            pinfl = saved.pinfl,
            hireDate = saved.hireDate,
            organizationId = saved.organization.id!!,
        )
    }

    fun getAll(): List<EmployeeResponse> =
        employeeRepository.findAll().map {
            EmployeeResponse(
                id = it.id!!,
                firstName = it.firstName,
                lastName = it.lastName,
                pinfl = it.pinfl,
                hireDate = it.hireDate,
                organizationId = it.organization.id!!
            )
        }
}

@Service
class OrganizationService(
    private val organizationRepository: OrganizationRepository,
    private val regionRepository: RegionRepository
) {
    fun create(req: OrganizationCreateRequest): OrganizationResponse {
        val region = regionRepository.findById(req.regionId)
            .orElseThrow { IllegalArgumentException("Region not found") }

        val parent = req.parentId?.let {
            organizationRepository.findById(it)
                .orElseThrow { IllegalArgumentException("Parent organization not found") }
        }

        val saved = organizationRepository.save(
            Organization(
                name = req.name,
                region = region,
                parent = parent
            )
        )

        return OrganizationResponse(
            id = saved.id!!,
            name = saved.name,
            regionId = saved.region.id!!,
            parentId = saved.parent?.id
        )
    }

    fun getAll(): List<OrganizationResponse> =
        organizationRepository.findAll().map {
            OrganizationResponse(
                id = it.id!!,
                name = it.name,
                regionId = it.region.id!!,
                parentId = it.parent?.id
            )
        }
}

@Service
class RegionService(
    private val regionRepository: RegionRepository
) {
    fun create(req: RegionCreateRequest): RegionResponse {
        val saved = regionRepository.save(
            Region(name = req.name)
        )
        return RegionResponse(id = saved.id!!, name = saved.name)
    }

    fun getAll(): List<RegionResponse> =
        regionRepository.findAll().map {
            RegionResponse(id = it.id!!, name = it.name)
        }
}

@Service
class CalculationService(
    private val calcRepo: CalculationTableRepository,
    private val employeeRepository: EmployeeRepository,
    private val organizationRepository: OrganizationRepository
) {
    fun create(req: CalculationCreateRequest): CalculationResponse {

        val employee = employeeRepository.findById(req.employeeId)
            .orElseThrow { IllegalArgumentException("Employee not found") }

        val org = organizationRepository.findById(req.organizationId)
            .orElseThrow { IllegalArgumentException("Organization not found") }


        val saved = calcRepo.save(
            CalculationTable(
                employee = employee,
                amount = req.amount,
                rate = req.rate,
                date = req.date,
                organization = org,
                calculationType = req.calculationType
            )
        )

        return CalculationResponse(
            id = saved.id!!,
            employeeId = saved.employee.id!!,
            amount = saved.amount,
            rate = saved.rate,
            date = saved.date,
            organizationId = saved.organization.id!!,
            calculationType = saved.calculationType
        )
    }
}

@Service
@Transactional(readOnly = true)
class StatisticsService(
    private val calcRepo: CalculationTableRepository,
    private val dateRangeParser: DateRangeParser
) {

    fun getEmployeesWithRateGreaterThan(
        month: String,
        minRate: BigDecimal
    ): List<OverRateEmployeeDto> {
        val (start, end) = dateRangeParser.monthToRange(month)
        return calcRepo.findEmployeesWithTotalRateGreaterThan(start, end, minRate)
    }


    fun getEmployeesWorkedInDifferentRegions(month: String): List<MultiRegionEmployeeDto> {
        val (start, end) = dateRangeParser.monthToRange(month)
        return calcRepo.findEmployeesWorkedInDifferentRegions(start, end)
    }

    fun getOrganizationAverageSalaries(
        month: String,
        organizationId: Long
    ): List<OrganizationAverageSalaryDto> {
        val (start, end) = dateRangeParser.monthToRange(month)
        return calcRepo.findOrganizationAverageSalariesForOrgAndChildren(start, end, organizationId)
    }

    fun getEmployeesWithSalaryAndVacation(month: String): List<SalaryAndVacationDto> {
        val (start, end) = dateRangeParser.monthToRange(month)
        return calcRepo.findEmployeesWithSalaryAndVacation(start, end)
    }
}
