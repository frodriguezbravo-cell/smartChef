package es.safareyes.smartchef.servicios.calculos;

import es.safareyes.smartchef.dtos.ResumenNutricional;
import es.safareyes.smartchef.modelos.Ingrediente;
import es.safareyes.smartchef.modelos.Receta;
import es.safareyes.smartchef.modelos.RecetaIngrediente;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Calcula la nutrición de una receta a partir de sus ingredientes (valores por 100 g).
 * Mismo criterio que la vista v_receta_nutricion: no cuentan los ingredientes opcionales.
 * A diferencia de la vista, avisa (completo = false) si algún ingrediente no tiene datos.
 */
public final class CalculadoraNutricion {

    private static final BigDecimal CIEN = BigDecimal.valueOf(100);

    private CalculadoraNutricion() {
    }

    /** Nutrición de UNA ración. */
    public static ResumenNutricional porRacion(Receta receta) {
        return paraRaciones(receta, 1);
    }

    /** Nutrición del total de las raciones indicadas (por ejemplo, 3 raciones de una receta de 2). */
    public static ResumenNutricional paraRaciones(Receta receta, int racionesDeseadas) {
        if (racionesDeseadas <= 0) {
            throw new IllegalArgumentException("Las raciones deben ser mayores que 0");
        }
        if (receta.getRaciones() == null || receta.getRaciones() <= 0) {
            throw new IllegalArgumentException("La receta no tiene un número de raciones válido");
        }

        BigDecimal kcal = BigDecimal.ZERO;
        BigDecimal proteinas = BigDecimal.ZERO;
        BigDecimal hidratos = BigDecimal.ZERO;
        BigDecimal azucares = BigDecimal.ZERO;
        BigDecimal grasas = BigDecimal.ZERO;
        BigDecimal saturadas = BigDecimal.ZERO;
        BigDecimal fibra = BigDecimal.ZERO;
        BigDecimal sal = BigDecimal.ZERO;
        int sinDatos = 0;

        for (RecetaIngrediente linea : receta.getIngredientes()) {
            if (Boolean.TRUE.equals(linea.getEsOpcional())) {
                continue;
            }
            Ingrediente ingrediente = linea.getIngrediente();
            if (linea.getCantidadGramos() == null || ingrediente == null || ingrediente.getCaloriasKcal() == null) {
                sinDatos++;
                continue;
            }
            BigDecimal factor = linea.getCantidadGramos().divide(CIEN);   // gramos / 100
            kcal = suma(kcal, ingrediente.getCaloriasKcal(), factor);
            proteinas = suma(proteinas, ingrediente.getProteinasG(), factor);
            hidratos = suma(hidratos, ingrediente.getHidratosG(), factor);
            azucares = suma(azucares, ingrediente.getAzucaresG(), factor);
            grasas = suma(grasas, ingrediente.getGrasasG(), factor);
            saturadas = suma(saturadas, ingrediente.getGrasasSaturadasG(), factor);
            fibra = suma(fibra, ingrediente.getFibraG(), factor);
            sal = suma(sal, ingrediente.getSalG(), factor);
        }

        BigDecimal multiplicador = BigDecimal.valueOf(racionesDeseadas);
        BigDecimal racionesReceta = BigDecimal.valueOf(receta.getRaciones());

        ResumenNutricional resumen = new ResumenNutricional();
        resumen.setCaloriasKcal(ajustar(kcal, multiplicador, racionesReceta, 1));
        resumen.setProteinasG(ajustar(proteinas, multiplicador, racionesReceta, 1));
        resumen.setHidratosG(ajustar(hidratos, multiplicador, racionesReceta, 1));
        resumen.setAzucaresG(ajustar(azucares, multiplicador, racionesReceta, 1));
        resumen.setGrasasG(ajustar(grasas, multiplicador, racionesReceta, 1));
        resumen.setGrasasSaturadasG(ajustar(saturadas, multiplicador, racionesReceta, 1));
        resumen.setFibraG(ajustar(fibra, multiplicador, racionesReceta, 1));
        resumen.setSalG(ajustar(sal, multiplicador, racionesReceta, 2));
        resumen.setCompleto(sinDatos == 0);
        resumen.setLineasSinDatos(sinDatos);
        return resumen;
    }

    private static BigDecimal suma(BigDecimal acumulado, BigDecimal valorPor100g, BigDecimal factor) {
        return valorPor100g == null ? acumulado : acumulado.add(valorPor100g.multiply(factor));
    }

    /** total * raciones deseadas / raciones de la receta, redondeado una sola vez al final. */
    private static BigDecimal ajustar(BigDecimal total, BigDecimal multiplicador, BigDecimal racionesReceta, int decimales) {
        return total.multiply(multiplicador).divide(racionesReceta, decimales, RoundingMode.HALF_UP);
    }
}
