package com.example.payrollanalytics


import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/employees")
@Tag(name = "Employees")
class EmployeesController(
    private val employeeService: EmployeeService
) {

    @PostMapping
    @Operation(summary = "Creates an employee")
    fun create(@RequestBody req: EmployeeCreateRequest): EmployeeResponse = employeeService.create(req)

    @GetMapping
    @Operation(summary = "Get all employees")
    fun getAll(): List<EmployeeResponse> = employeeService.getAll()
}

@RestController
@RequestMapping("/api/organizations")
@Tag(name = "Organizations")
class OrganizationController(
    private val organizationService: OrganizationService
) {
    @PostMapping
    @Operation(summary = "Creates an organization")
    fun create(@RequestBody req: OrganizationCreateRequest): OrganizationResponse =
        organizationService.create(req)

    @GetMapping
    @Operation(summary = "Get all organizations")
    fun getAll(): List<OrganizationResponse> = organizationService.getAll()
}

@RestController
@RequestMapping("/api/regions")
@Tag(name = "Regions")
class RegionController(
    private val regionService: RegionService
) {
    @PostMapping
    @Operation(summary = "Create region")
    fun create(@RequestBody req: RegionCreateRequest): RegionResponse =
        regionService.create(req)

    @GetMapping
    @Operation(summary = "Get all regions")
    fun getAll(): List<RegionResponse> = regionService.getAll()
}

@RestController
@RequestMapping("/api/calculations")
@Tag(name = "Calculations")
class CalculationController(
    private val calculationService: CalculationService
) {
    @PostMapping
    @Operation(summary = "Create calculation row")
    fun create(@RequestBody req: CalculationCreateRequest): CalculationResponse =
        calculationService.create(req)
}

@RestController
@RequestMapping("/api/statistics")
@Tag(name = "Statistics")
class StatisticsController(
    private val statisticsService: StatisticsService
) {
    @GetMapping("/over-rate")
    @Operation(summary = "Employees whose total rate > 1 in given month")
    fun getOverRate(@RequestParam month: String): List<OverRateEmployeeDto> =
        statisticsService.getEmployeesWithRateGreaterThanOne(month)


    @GetMapping("/multi-region")
    @Operation(summary = "Employees worked in different regions in given month")
    fun getMultiRegion(@RequestParam month: String): List<MultiRegionEmployeeDto> =
        statisticsService.getEmployeesWorkedInDifferentRegions(month)

    @GetMapping("/org-average")
    @Operation(summary = "Average monthly salary for organization and its children")
    fun getOrgAverage(
        @RequestParam month: String,
        @RequestParam organizationId: Long
    ): List<OrganizationAverageSalaryDto> =
        statisticsService.getOrganizationAverageSalaries(month, organizationId)

    @GetMapping("/salary-vacation")
    @Operation(summary = "Employees with salary AND vacation in given month")
    fun getSalaryAndVacation(@RequestParam month: String): List<SalaryAndVacationDto> =
        statisticsService.getEmployeesWithSalaryAndVacation(month)
}
