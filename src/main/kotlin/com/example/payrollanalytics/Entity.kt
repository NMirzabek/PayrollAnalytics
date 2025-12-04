package com.example.payrollanalytics

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDate

@Entity
@Table(name = "employee")
class Employee(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "first_name", nullable = false)
    var firstName: String,

    @Column(name = "last_name", nullable = false)
    var lastName: String,

    @Column(nullable = false) // pinfl unique bolmasligi kerak
    var pinfl: String,

    @Column(name = "hire_date", nullable = false)
    var hireDate: LocalDate,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    var organization: Organization
)
//{
//    constructor() : this(
//        id = null,
//        firstName = "",
//        lastName = "",
//        pinfl = "",
//        hireDate = LocalDate.now(),
//        organization = Organization()
//    )
//}

@Entity
@Table(name = "organization")
class Organization(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false)
    var name: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id")
    var region: Region,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent")
    var parent: Organization? = null
)
//{
//    constructor() : this(
//        id = null,
//        name = "",
//        region = Region(),
//        parent = null
//    )
//}

@Entity
@Table(name = "region")
class Region(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false)
    var name: String
)
//{
//    constructor() : this(
//        id = null,
//        name = ""
//    )
//}

@Entity
@Table(name = "calculation_table")
class CalculationTable(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    var employee: Employee,

    @Column(nullable = false)
    var amount: BigDecimal,

    @Column(nullable = false)
    var rate: BigDecimal,

    @Column(nullable = false)
    var date: LocalDate,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    var organization: Organization,

    @Enumerated(EnumType.STRING)
    @Column(name = "calculation_type", nullable = false)
    var calculationType: CalculationType
)
//{
//    constructor() : this(
//        id = null,
//        employee = Employee(),
//        amount = BigDecimal.ZERO,
//        rate = BigDecimal.ONE,
//        date = LocalDate.now(),
//        organization = Organization(),
//        calculationType = CalculationType.SALARY
//    )
//}
