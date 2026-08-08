package com.dunamis.sistema.exception;

public class InvalidPasswordException extends RuntimeException {

    public InvalidPasswordException() {
        super("A password actual está incorrecta.");
    }
}
