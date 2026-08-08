package com.dunamis.sistema.exception;

public class DuplicateEmailException extends RuntimeException {

    public DuplicateEmailException() {
        super("Este email já está cadastrado.");
    }
}
