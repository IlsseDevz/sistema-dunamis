package com.dunamis.sistema.exception;

public class DuplicateGradeException extends RuntimeException {

    public DuplicateGradeException() {
        super("Já existe uma nota deste tipo para este aluno nesta disciplina.");
    }
}
