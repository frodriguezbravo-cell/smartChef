package es.safareyes.smartchef.excepciones;

/** Se lanza cuando se incumple una regla de la aplicación (raciones negativas, dietas incompatibles...). El controlador la convertirá en un 400. */
public class ReglaNegocioException extends RuntimeException {

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
