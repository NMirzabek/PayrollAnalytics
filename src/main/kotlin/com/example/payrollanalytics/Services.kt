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
    private val orgRepo: OrganizationRepository,
    private val dateRangeParser: DateRangeParser
) {
    fun getEmployeesWithRateGreaterThanOne(month: String): List<OverRateEmployeeDto> {
        val (start, end) = dateRangeParser.monthToRange(month)
        val calcs = calcRepo.findAllByDateBetween(start, end)

        val grouped = calcs.groupBy { it.employee.pinfl }

        return grouped.mapNotNull { (pinfl, list) ->
            val totalRate = list.fold(BigDecimal.ZERO) { acc, c -> acc.add(c.rate) }
            if (totalRate > BigDecimal.ONE) {
                OverRateEmployeeDto(pinfl = pinfl, totalRate = totalRate)
            } else null
        }
    }

    fun getEmployeesWorkedInDifferentRegions(month: String): List<MultiRegionEmployeeDto> {
        val (start, end) = dateRangeParser.monthToRange(month)
        val salaries = calcRepo.findAllByDateBetweenAndCalculationType(start, end, CalculationType.SALARY)

        val grouped = salaries.groupBy { it.employee.pinfl }

        return grouped.mapNotNull { (pinfl, list) ->
            val regionIds = list.map { it.organization.region.id }.toSet()
            if (regionIds.size <= 1) return@mapNotNull null

            val orgCount = list.map { it.organization.id }.toSet().size.toLong()
            val totalSalary = list.fold(BigDecimal.ZERO) { acc, c -> acc.add(c.amount) }

            MultiRegionEmployeeDto(
                pinfl = pinfl,
                organizationCount = orgCount,
                totalSalary = totalSalary
            )
        }
    }


    fun getOrganizationAverageSalaries(month: String, organizationId: Long): List<OrganizationAverageSalaryDto> {
        val (start, end) = dateRangeParser.monthToRange(month)

        val root = orgRepo.findById(organizationId)
            .orElseThrow { IllegalArgumentException("Organization not found") }


        val children = orgRepo.findAllByParent_Id(organizationId)
        val organizations = listOf(root) + children

        val allCalcs = calcRepo.findAllByDateBetweenAndCalculationType(start, end, CalculationType.SALARY)
            .filter { it.organization in organizations }

        val grouped = allCalcs.groupBy { it.employee }

        return grouped.map { (employee, list) ->
            val total = list.fold(BigDecimal.ZERO) { acc, c -> acc.add(c.amount) }
            val avg = total.divide(
                BigDecimal(list.size),
                2,
                RoundingMode.HALF_UP
            )

            OrganizationAverageSalaryDto(
                organizationId = employee.organization.id!!,
                organizationName = employee.organization.name,
                employeeId = employee.id!!,
                employeeFullName = "${employee.firstName} ${employee.lastName}",
                averageSalary = avg
            )
        }
    }

    fun getEmployeesWithSalaryAndVacation(month: String): List<SalaryAndVacationDto> {
        val (start, end) = dateRangeParser.monthToRange(month)
        val allCalcs = calcRepo.findAllByDateBetween(start, end)

        val grouped = allCalcs.groupBy { it.employee }

        return grouped.mapNotNull { (employee, list) ->
            val salary = list
                .filter { it.calculationType == CalculationType.SALARY }
                .fold(BigDecimal.ZERO) { acc, c -> acc.add(c.amount) }


            val vacation = list
                .filter { it.calculationType == CalculationType.VACATION }
                .fold(BigDecimal.ZERO) { acc, c -> acc.add(c.amount) }


            if (salary == BigDecimal.ZERO || vacation == BigDecimal.ZERO) return@mapNotNull null

            SalaryAndVacationDto(
                employeeId = employee.id!!,
                fullName = "${employee.firstName} ${employee.lastName}",
                pinfl = employee.pinfl,
                salaryAmount = salary,
                vacationAmount = vacation
            )
        }
    }
}
