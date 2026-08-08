package com.dunamis.sistema.exception;

public class DuplicateContactException extends RuntimeException {

    public DuplicateContactException() {
        super("Este contacto já está cadastrado.");
    }
}
