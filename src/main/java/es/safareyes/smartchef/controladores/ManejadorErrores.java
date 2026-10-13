package es.safareyes.smartchef.controladores;

import es.safareyes.smartchef.excepciones.RecursoNoEncontradoException;
import es.safareyes.smartchef.excepciones.ReglaNegocioException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Convierte las excepciones en respuestas de error con el formato estándar ProblemDetail (RFC 9457):
 * { "title": "...", "status": 404, "detail": "Receta no encontrado/a: 7" }
 * Solo se tratan los errores propios y los de petición mal formada; el resto (404 de ruta, 405...) lo gestiona Spring.
 */
@RestControllerAdvice
public class ManejadorErrores {

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ProblemDetail noEncontrado(RecursoNoEncontradoException ex) {
        return problema(HttpStatus.NOT_FOUND, "Recurso no encontrado", ex.getMessage());
    }

    @ExceptionHandler(ReglaNegocioException.class)
    public ProblemDetail reglaDeNegocio(ReglaNegocioException ex) {
        return problema(HttpStatus.BAD_REQUEST, "Petición no válida", ex.getMessage());
    }

    /** Falla @Valid: devuelve en "errores" qué campo falló y por qué. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail datosNoValidos(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errores.putIfAbsent(error.getField(), error.getDefaultMessage());
        }
        ProblemDetail problema = problema(HttpStatus.BAD_REQUEST, "Datos no válidos", "Hay campos con valores no válidos");
        problema.setProperty("errores", errores);
        return problema;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail cuerpoIlegible(HttpMessageNotReadableException ex) {
        return problema(HttpStatus.BAD_REQUEST, "Petición no válida", "El cuerpo de la petición no es un JSON válido o tiene valores del tipo incorrecto");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail tipoIncorrecto(MethodArgumentTypeMismatchException ex) {
        return problema(HttpStatus.BAD_REQUEST, "Petición no válida", "El valor del parámetro \"" + ex.getName() + "\" no es válido");
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ProblemDetail faltaParametro(MissingServletRequestParameterException ex) {
        return problema(HttpStatus.BAD_REQUEST, "Petición no válida", "Falta el parámetro \"" + ex.getParameterName() + "\"");
    }

    private static ProblemDetail problema(HttpStatus estado, String titulo, String detalle) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(estado, detalle);
        problema.setTitle(titulo);
        return problema;
    }
}
