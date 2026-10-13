package es.safareyes.smartchef.servicios.calculos;

import es.safareyes.smartchef.dtos.IngredienteEscalado;
import es.safareyes.smartchef.modelos.Receta;
import es.safareyes.smartchef.pruebas.Datos;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static es.safareyes.smartchef.pruebas.Datos.bd;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EscaladorTest {

    private static void igual(String esperado, BigDecimal real) {
        assertEquals(0, bd(esperado).compareTo(real), "esperado " + esperado + " pero fue " + real);
    }

    @Test
    void elFactorEsRacionesDeseadasEntreRacionesDeLaReceta() {
        Receta receta = Datos.espaguetisAlPomodoro();           // 2 raciones
        igual("1.5", Escalador.factor(receta, 3));
        igual("0.5", Escalador.factor(receta, 1));
        igual("2", Escalador.factor(receta, 4));
    }

    @Test
    void escalaLasCantidadesATresRaciones() {
        List<IngredienteEscalado> lista = Escalador.escalar(Datos.espaguetisAlPomodoro(), 3);
        IngredienteEscalado pasta = lista.get(0);
        assertEquals("Espaguetis", pasta.getNombre());
        igual("300", pasta.getCantidad());
        igual("600", lista.get(1).getCantidad());               // tomate
        igual("3", lista.get(2).getCantidad());                 // ajo
        igual("27", lista.get(3).getCantidadGramos());          // aceite: 18 g * 1,5
        assertEquals("G", pasta.getUnidadCodigo());
    }

    @Test
    void marcaLosIngredientesOpcionales() {
        List<IngredienteEscalado> lista = Escalador.escalar(Datos.espaguetisAlPomodoro(), 2);
        assertEquals(5, lista.size());
        assertTrue(lista.get(4).getOpcional());
        assertEquals(false, lista.get(0).getOpcional());
    }

    @Test
    void lasLineasAlGustoSeQuedanSinCantidad() {
        Receta receta = Datos.espaguetisAlPomodoro();
        Datos.linea(receta, Datos.ingrediente(9, "Sal", "0", "0", "0", "0", "0", "0", "0", "100"),
                null, Datos.unidad(10, "AL_GUSTO", "al gusto"), null, false);
        IngredienteEscalado sal = Escalador.escalar(receta, 6).get(5);
        assertNull(sal.getCantidad());
        assertEquals("AL_GUSTO", sal.getUnidadCodigo());
    }

    @Test
    void rechazaRacionesNoValidas() {
        Receta receta = Datos.espaguetisAlPomodoro();
        assertThrows(IllegalArgumentException.class, () -> Escalador.escalar(receta, 0));
    }
}
