package es.safareyes.smartchef.excepciones;

/** Se lanza cuando no existe lo que se pide (usuario, receta, despensa...). El controlador la convertirá en un 404. */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }

    /** Mensaje estándar: "Receta no encontrado/a: 7". */
    public RecursoNoEncontradoException(String recurso, Object identificador) {
        super(recurso + " no encontrado/a: " + identificador);
    }
}
