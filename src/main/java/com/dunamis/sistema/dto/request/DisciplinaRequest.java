package com.dunamis.sistema.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class DisciplinaRequest {

    @NotBlank(message = "O nome da disciplina é obrigatório.")
    @Size(max = 150, message = "O nome não pode exceder 150 caracteres.")
    private String name;

    @Size(max = 500, message = "A descrição não pode exceder 500 caracteres.")
    private String description;

    private Integer workloadHours;

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

    public Integer getWorkloadHours() {
        return workloadHours;
    }

    public void setWorkloadHours(Integer workloadHours) {
        this.workloadHours = workloadHours;
    }
}
