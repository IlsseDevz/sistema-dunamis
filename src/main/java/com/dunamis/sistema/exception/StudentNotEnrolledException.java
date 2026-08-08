package com.dunamis.sistema.exception;

public class StudentNotEnrolledException extends RuntimeException {

    public StudentNotEnrolledException() {
        super("Não está inscrito na Escola Bíblica.");
    }
}
