package com.example.payrollanalytics

import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@Component
class DateRangeParser {

    fun monthToRange(month: String): Pair<LocalDate, LocalDate> {
        val formatter = DateTimeFormatter.ofPattern("yyyy.MM")

        val yearMonth = YearMonth.parse(month, formatter)

        val firstDay = yearMonth.atDay(1)
        val endExclusive = yearMonth.plusMonths(1).atDay(1)

        return firstDay to endExclusive
    }
}
