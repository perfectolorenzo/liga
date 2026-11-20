package es.edu.liga.domain.exception;

public class EquipoNotFoundException extends RuntimeException {
    public EquipoNotFoundException(Long id) {
        super("No existe el equipo con ID: " + id);
    }
}
