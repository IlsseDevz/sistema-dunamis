package com.dunamis.sistema.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class ReportPeriodRequest {

    @NotNull(message = "Seleccione o mês.")
    @Min(value = 1, message = "Mês inválido.")
    @Max(value = 12, message = "Mês inválido.")
    private Integer month;

    @NotNull(message = "Seleccione o ano.")
    @Min(value = 2000, message = "Ano inválido.")
    @Max(value = 2100, message = "Ano inválido.")
    private Integer year;

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }
}
