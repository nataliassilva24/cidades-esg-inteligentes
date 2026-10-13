package br.com.fiap.healthtrack.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public static ResourceNotFoundException comId(String recurso, Long id) {
        return new ResourceNotFoundException(recurso + " não encontrado com id: " + id);
    }
}
