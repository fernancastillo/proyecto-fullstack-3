package com.bffservice.exception;

public class ServiceUnavailableException extends RuntimeException {
    public ServiceUnavailableException(String service) {
        super("El servicio de " + service + " no está disponible en este momento. Por favor, intente nuevamente más tarde.");
    }
}