package com.dunamis.sistema.entity.enums;

public enum TurmaStatus {
    ACTIVE("Ativa"),
    INACTIVE("Inativa");

    private final String label;

    TurmaStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
