package com.dunamis.sistema.entity.enums;

public enum AccountStatus {
    ACTIVE("Ativa"),
    INACTIVE("Inativa");

    private final String label;

    AccountStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
