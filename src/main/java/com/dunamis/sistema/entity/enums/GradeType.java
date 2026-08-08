package com.dunamis.sistema.entity.enums;

/**
 * Tipos de avaliação — estrutura preparada para expansão futura.
 * Na primeira versão, pode ser usado principalmente AVALIACAO_1 ou NOTA_UNICA.
 */
public enum GradeType {
    AVALIACAO_1("Avaliação 1"),
    AVALIACAO_2("Avaliação 2"),
    TRABALHO("Trabalho"),
    EXAME("Exame"),
    NOTA_UNICA("Nota"),
    MEDIA_FINAL("Média final");

    private final String label;

    GradeType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
