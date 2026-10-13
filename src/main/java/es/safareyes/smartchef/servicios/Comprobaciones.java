package es.safareyes.smartchef.servicios;

import es.safareyes.smartchef.excepciones.RecursoNoEncontradoException;

import java.util.Optional;

/** Utilidades comunes de los servicios. */
final class Comprobaciones {

    private Comprobaciones() {
    }

    /** Devuelve el valor o lanza RecursoNoEncontradoException ("Receta no encontrado/a: 7"). */
    static <T> T existe(Optional<T> valor, String recurso, Object identificador) {
        return valor.orElseThrow(() -> new RecursoNoEncontradoException(recurso, identificador));
    }
}
