package com.dunamis.sistema.dto.request;

import com.dunamis.sistema.entity.enums.GradeType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class GradeRequest {

    @NotNull(message = "Seleccione o aluno.")
    private Long inscricaoId;

    @NotNull(message = "Seleccione a disciplina.")
    private Long disciplinaId;

    @NotNull(message = "Seleccione o tipo de avaliação.")
    private GradeType gradeType;

    @NotNull(message = "A nota é obrigatória.")
    @DecimalMin(value = "0.0", message = "A nota mínima é 0.")
    @DecimalMax(value = "20.0", message = "A nota máxima é 20.")
    private BigDecimal value;

    @Size(max = 500, message = "A observação não pode exceder 500 caracteres.")
    private String observation;

    public Long getInscricaoId() {
        return inscricaoId;
    }

    public void setInscricaoId(Long inscricaoId) {
        this.inscricaoId = inscricaoId;
    }

    public Long getDisciplinaId() {
        return disciplinaId;
    }

    public void setDisciplinaId(Long disciplinaId) {
        this.disciplinaId = disciplinaId;
    }

    public GradeType getGradeType() {
        return gradeType;
    }

    public void setGradeType(GradeType gradeType) {
        this.gradeType = gradeType;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public String getObservation() {
        return observation;
    }

    public void setObservation(String observation) {
        this.observation = observation;
    }
}
