package es.safareyes.smartchef.controladores;

import es.safareyes.smartchef.excepciones.RecursoNoEncontradoException;
import es.safareyes.smartchef.excepciones.ReglaNegocioException;
import org.junit.jupiter.api.Test;
import org.springframework.http.ProblemDetail;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ManejadorErroresTest {

    private final ManejadorErrores manejador = new ManejadorErrores();

    @Test
    void unRecursoQueNoExisteEsUn404ConElMensajeDelServicio() {
        ProblemDetail problema = manejador.noEncontrado(new RecursoNoEncontradoException("Receta", 7));
        assertEquals(404, problema.getStatus());
        assertEquals("Receta no encontrado/a: 7", problema.getDetail());
        assertEquals("Recurso no encontrado", problema.getTitle());
    }

    @Test
    void unaReglaDeNegocioIncumplidaEsUn400() {
        ProblemDetail problema = manejador.reglaDeNegocio(new ReglaNegocioException("Las raciones deben ser mayores que 0"));
        assertEquals(400, problema.getStatus());
        assertEquals("Las raciones deben ser mayores que 0", problema.getDetail());
    }
}
