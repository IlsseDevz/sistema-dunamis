package com.dunamis.sistema.exception;

public class DisciplinaInUseException extends RuntimeException {

    public DisciplinaInUseException() {
        super("Não é possível remover uma disciplina que possui notas registadas.");
    }
}
