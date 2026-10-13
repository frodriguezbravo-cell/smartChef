package es.safareyes.smartchef.servicios.calculos;

import es.safareyes.smartchef.dtos.ResumenNutricional;
import es.safareyes.smartchef.modelos.Receta;
import es.safareyes.smartchef.pruebas.Datos;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static es.safareyes.smartchef.pruebas.Datos.bd;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CalculadoraNutricionTest {

    /** Compara BigDecimal por valor (2.0 y 2.00 son iguales). */
    private static void igual(String esperado, BigDecimal real) {
        assertEquals(0, bd(esperado).compareTo(real), "esperado " + esperado + " pero fue " + real);
    }

    @Test
    void nutricionPorRacionDeLosEspaguetis() {
        ResumenNutricional n = CalculadoraNutricion.porRacion(Datos.espaguetisAlPomodoro());
        // Mismos valores que devuelve la vista v_receta_nutricion del script SQL
        igual("480.5", n.getCaloriasKcal());
        igual("14.6", n.getProteinasG());
        igual("81.1", n.getHidratosG());
        igual("8.7", n.getAzucaresG());
        igual("10.9", n.getGrasasG());
        igual("1.6", n.getGrasasSaturadasG());
        igual("5.5", n.getFibraG());
        igual("0.03", n.getSalG());
        assertTrue(n.getCompleto());
        assertEquals(0, n.getLineasSinDatos());
    }

    @Test
    void elIngredienteOpcionalNoCuenta() {
        // Sin parmesano (opcional) son 480,5 kcal; si contara, serían 20 g * 3,92 / 2 = 39,2 kcal más
        ResumenNutricional n = CalculadoraNutricion.porRacion(Datos.espaguetisAlPomodoro());
        assertFalse(n.getCaloriasKcal().compareTo(bd("500")) > 0);
    }

    @Test
    void paraMasRacionesSeEscalaElTotal() {
        // 961,04 kcal en total para 2 raciones -> 4 raciones = 1922,08 kcal
        ResumenNutricional n = CalculadoraNutricion.paraRaciones(Datos.espaguetisAlPomodoro(), 4);
        igual("1922.1", n.getCaloriasKcal());
    }

    @Test
    void unaRacionEsLaMitadDeLasDosRaciones() {
        Receta receta = Datos.espaguetisAlPomodoro();
        ResumenNutricional total = CalculadoraNutricion.paraRaciones(receta, 2);
        ResumenNutricional una = CalculadoraNutricion.porRacion(receta);
        igual("961.0", total.getCaloriasKcal());
        igual("480.5", una.getCaloriasKcal());
    }

    @Test
    void avisaSiFaltanDatosDeUnIngrediente() {
        Receta receta = Datos.espaguetisAlPomodoro();
        Datos.linea(receta, Datos.ingrediente(9, "Sal", "0", "0", "0", "0", "0", "0", "0", "100"),
                null, Datos.unidad(10, "AL_GUSTO", "al gusto"), null, false);
        ResumenNutricional n = CalculadoraNutricion.porRacion(receta);
        assertFalse(n.getCompleto());
        assertEquals(1, n.getLineasSinDatos());
        igual("480.5", n.getCaloriasKcal());
    }

    @Test
    void rechazaRacionesNoValidas() {
        Receta receta = Datos.espaguetisAlPomodoro();
        assertThrows(IllegalArgumentException.class, () -> CalculadoraNutricion.paraRaciones(receta, 0));
        receta.setRaciones((short) 0);
        assertThrows(IllegalArgumentException.class, () -> CalculadoraNutricion.porRacion(receta));
    }
}
