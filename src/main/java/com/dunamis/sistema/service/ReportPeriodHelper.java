package com.dunamis.sistema.service;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Component
public class ReportPeriodHelper {

    private static final DateTimeFormatter PERIOD_FORMAT =
            DateTimeFormatter.ofPattern("MMMM yyyy", new Locale("pt", "PT"));

    public YearMonth toYearMonth(int month, int year) {
        return YearMonth.of(year, month);
    }

    public LocalDate startDate(YearMonth yearMonth) {
        return yearMonth.atDay(1);
    }

    public LocalDate endDate(YearMonth yearMonth) {
        return yearMonth.atEndOfMonth();
    }

    public LocalDateTime startDateTime(YearMonth yearMonth) {
        return yearMonth.atDay(1).atStartOfDay();
    }

    public LocalDateTime endDateTime(YearMonth yearMonth) {
        return yearMonth.atEndOfMonth().atTime(23, 59, 59);
    }

    public String formatPeriod(YearMonth yearMonth) {
        return yearMonth.atDay(1).format(PERIOD_FORMAT);
    }
}
