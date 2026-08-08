package com.dunamis.sistema.exception;

public class DuplicateDisciplinaException extends RuntimeException {

    public DuplicateDisciplinaException() {
        super("Já existe uma disciplina com este nome nesta turma.");
    }
}
