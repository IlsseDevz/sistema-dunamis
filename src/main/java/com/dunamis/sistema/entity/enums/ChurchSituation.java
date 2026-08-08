package com.dunamis.sistema.entity.enums;

public enum ChurchSituation {
    MEMBRO_ATIVO("Membro ativo"),
    NOVO_CONVERTIDO("Novo convertido"),
    VISITANTE("Visitante");

    private final String label;

    ChurchSituation(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
