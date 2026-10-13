package es.safareyes.smartchef.servicios.calculos;

import es.safareyes.smartchef.modelos.DespensaItem;
import es.safareyes.smartchef.modelos.Ingrediente;
import es.safareyes.smartchef.modelos.Receta;
import es.safareyes.smartchef.modelos.catalogos.UnidadMedida;
import es.safareyes.smartchef.pruebas.Datos;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static es.safareyes.smartchef.pruebas.Datos.bd;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CalculadoraCompraTest {

    private final Receta receta = Datos.espaguetisAlPomodoro();            // 2 raciones
    private final Ingrediente pasta = receta.getIngredientes().get(0).getIngrediente();
    private final Ingrediente tomate = receta.getIngredientes().get(1).getIngrediente();
    private final UnidadMedida gramos = receta.getIngredientes().get(0).getUnidad();
    private final UnidadMedida kilos = Datos.unidad(2, "KG", "kilos (kg)");

    private static Optional<CalculadoraCompra.Faltante> de(List<CalculadoraCompra.Faltante> lista, Ingrediente ingrediente) {
        return lista.stream().filter(f -> f.ingrediente().getId().equals(ingrediente.getId())).findFirst();
    }

    @Test
    void conLaDespensaVaciaHayQueComprarTodoLoObligatorio() {
        List<CalculadoraCompra.Faltante> lista =
                CalculadoraCompra.faltantes(receta.getIngredientes(), BigDecimal.ONE, List.of());
        assertEquals(4, lista.size());                                       // el parmesano es opcional
        assertEquals(0, bd("400.00").compareTo(de(lista, tomate).orElseThrow().cantidad()));
    }

    @Test
    void soloSeCompraLoQueFalta() {
        List<DespensaItem> despensa = List.of(
                Datos.stock(tomate, "300", gramos),                          // necesita 400: faltan 100
                Datos.stock(pasta, "500", gramos));                          // necesita 200: de sobra
        List<CalculadoraCompra.Faltante> lista =
                CalculadoraCompra.faltantes(receta.getIngredientes(), BigDecimal.ONE, despensa);
        assertEquals(0, bd("100").compareTo(de(lista, tomate).orElseThrow().cantidad()));
        assertTrue(de(lista, pasta).isEmpty());
        assertEquals(3, lista.size());                                       // tomate, ajo y aceite
    }

    @Test
    void elFactorDeRacionesCambiaLoQueFalta() {
        List<DespensaItem> despensa = List.of(Datos.stock(tomate, "300", gramos));
        // 4 raciones = factor 2: necesita 800 g de tomate, hay 300 -> faltan 500
        List<CalculadoraCompra.Faltante> lista =
                CalculadoraCompra.faltantes(receta.getIngredientes(), bd("2"), despensa);
        assertEquals(0, bd("500").compareTo(de(lista, tomate).orElseThrow().cantidad()));
    }

    @Test
    void siSumanVariosProductosDelMismoIngredienteSeCuentanJuntos() {
        List<DespensaItem> despensa = List.of(
                Datos.stock(tomate, "150", gramos), Datos.stock(tomate, "250", gramos));
        List<CalculadoraCompra.Faltante> lista =
                CalculadoraCompra.faltantes(receta.getIngredientes(), BigDecimal.ONE, despensa);
        assertTrue(de(lista, tomate).isEmpty());                             // 150 + 250 = 400
    }

    @Test
    void losProductosDadosDeBajaNoCuentan() {
        DespensaItem caducado = Datos.stock(tomate, "1000", gramos);
        caducado.setFechaBaja(LocalDate.now());
        List<CalculadoraCompra.Faltante> lista =
                CalculadoraCompra.faltantes(receta.getIngredientes(), BigDecimal.ONE, List.of(caducado));
        assertEquals(0, bd("400").compareTo(de(lista, tomate).orElseThrow().cantidad()));
    }

    @Test
    void siHayElIngredienteEnOtraUnidadSeDaPorCubierto() {
        List<DespensaItem> despensa = List.of(Datos.stock(tomate, "2", kilos));
        List<CalculadoraCompra.Faltante> lista =
                CalculadoraCompra.faltantes(receta.getIngredientes(), BigDecimal.ONE, despensa);
        assertTrue(de(lista, tomate).isEmpty());
    }

    @Test
    void lasLineasAlGustoSoloSeAnadenSiNoHayNada() {
        Ingrediente sal = Datos.ingrediente(9, "Sal", "0", "0", "0", "0", "0", "0", "0", "100");
        UnidadMedida alGusto = Datos.unidad(10, "AL_GUSTO", "al gusto");
        Datos.linea(receta, sal, null, alGusto, null, false);

        List<CalculadoraCompra.Faltante> sinSal =
                CalculadoraCompra.faltantes(receta.getIngredientes(), BigDecimal.ONE, List.of());
        assertNull(de(sinSal, sal).orElseThrow().cantidad());

        List<CalculadoraCompra.Faltante> conSal = CalculadoraCompra.faltantes(
                receta.getIngredientes(), BigDecimal.ONE, List.of(Datos.stock(sal, "1", gramos)));
        assertTrue(de(conSal, sal).isEmpty());
    }

    @Test
    void lasLineasRepetidasDelMismoIngredienteSeSumanAntesDeComparar() {
        // El tomate aparece dos veces (400 g + 100 g): con 450 g en la despensa faltan 50 g
        Datos.linea(receta, tomate, "100", gramos, "100", false);
        List<DespensaItem> despensa = List.of(Datos.stock(tomate, "450", gramos));
        List<CalculadoraCompra.Faltante> lista =
                CalculadoraCompra.faltantes(receta.getIngredientes(), BigDecimal.ONE, despensa);
        assertEquals(0, bd("50").compareTo(de(lista, tomate).orElseThrow().cantidad()));
    }
}
