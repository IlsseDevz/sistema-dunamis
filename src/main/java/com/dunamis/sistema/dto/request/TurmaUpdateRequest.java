package com.dunamis.sistema.dto.request;

import com.dunamis.sistema.entity.enums.TurmaStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class TurmaUpdateRequest {

    @NotBlank(message = "O nome da turma é obrigatório.")
    @Size(max = 150, message = "O nome não pode exceder 150 caracteres.")
    private String name;

    @Size(max = 500, message = "A descrição não pode exceder 500 caracteres.")
    private String description;

    @NotNull(message = "O ano é obrigatório.")
    private Integer year;

    @NotNull(message = "O status é obrigatório.")
    private TurmaStatus status;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public TurmaStatus getStatus() {
        return status;
    }

    public void setStatus(TurmaStatus status) {
        this.status = status;
    }
}
