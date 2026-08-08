package com.dunamis.sistema.exception;

public class AlreadyEnrolledException extends RuntimeException {

    public AlreadyEnrolledException() {
        super("Já está inscrito na Escola Bíblica.");
    }
}
