package com.dunamis.sistema.entity.enums;

public enum EnrollmentStatus {
    ACTIVE("Ativa"),
    INACTIVE("Inativa"),
    COMPLETED("Concluída");

    private final String label;

    EnrollmentStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
