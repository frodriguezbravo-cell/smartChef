package es.safareyes.smartchef.servicios.calculos;

import es.safareyes.smartchef.modelos.DespensaItem;
import es.safareyes.smartchef.modelos.Ingrediente;
import es.safareyes.smartchef.modelos.RecetaIngrediente;
import es.safareyes.smartchef.modelos.catalogos.UnidadMedida;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Calcula qué hay que comprar para una receta, teniendo en cuenta lo que ya hay en la despensa.
 * <ul>
 *   <li>Solo cuentan los ingredientes obligatorios.</li>
 *   <li>Las líneas del mismo ingrediente y unidad se suman antes de comparar.</li>
 *   <li>Solo se comparan cantidades en la MISMA unidad. Si en la despensa hay ese ingrediente pero
 *       en otra unidad, se da por cubierto (no se pueden convertir unidades sin saber el producto).</li>
 *   <li>Lo que no está en la despensa se añade completo; las líneas "al gusto" (sin cantidad) se
 *       añaden solo si no hay nada de ese ingrediente.</li>
 *   <li>Los productos dados de baja no cuentan.</li>
 * </ul>
 */
public final class CalculadoraCompra {

    private CalculadoraCompra() {
    }

    /** Lo que falta por comprar de un ingrediente. cantidad es null si la línea es "al gusto". */
    public record Faltante(Ingrediente ingrediente, UnidadMedida unidad, BigDecimal cantidad) {
    }

    private static final class Necesidad {
        private final Ingrediente ingrediente;
        private final UnidadMedida unidad;
        private BigDecimal cantidad;           // null mientras todas las líneas sean "al gusto"

        private Necesidad(Ingrediente ingrediente, UnidadMedida unidad) {
            this.ingrediente = ingrediente;
            this.unidad = unidad;
        }
    }

    public static List<Faltante> faltantes(List<RecetaIngrediente> lineas, BigDecimal factor,
                                           List<DespensaItem> despensa) {
        // 1) Agrupar las necesidades por ingrediente y unidad
        Map<String, Necesidad> necesidades = new LinkedHashMap<>();
        for (RecetaIngrediente linea : lineas) {
            if (Boolean.TRUE.equals(linea.getEsOpcional())) {
                continue;
            }
            String clave = linea.getIngrediente().getId() + "|" + idUnidad(linea.getUnidad());
            Necesidad n = necesidades.computeIfAbsent(clave,
                    k -> new Necesidad(linea.getIngrediente(), linea.getUnidad()));
            if (linea.getCantidad() != null) {
                BigDecimal escalada = linea.getCantidad().multiply(factor).setScale(2, RoundingMode.HALF_UP);
                n.cantidad = n.cantidad == null ? escalada : n.cantidad.add(escalada);
            }
        }

        // 2) Comparar con la despensa
        List<Faltante> faltantes = new ArrayList<>();
        for (Necesidad n : necesidades.values()) {
            List<DespensaItem> delIngrediente = new ArrayList<>();
            for (DespensaItem item : despensa) {
                if (item.getFechaBaja() == null
                        && Objects.equals(item.getIngrediente().getId(), n.ingrediente.getId())) {
                    delIngrediente.add(item);
                }
            }
            if (delIngrediente.isEmpty()) {
                faltantes.add(new Faltante(n.ingrediente, n.unidad, n.cantidad));
                continue;
            }
            if (n.cantidad == null) {
                continue;                                  // "al gusto" y ya lo tiene
            }
            BigDecimal disponible = BigDecimal.ZERO;
            boolean hayMismaUnidad = false;
            for (DespensaItem item : delIngrediente) {
                if (Objects.equals(idUnidad(item.getUnidad()), idUnidad(n.unidad))) {
                    hayMismaUnidad = true;
                    disponible = disponible.add(item.getCantidad());
                }
            }
            if (!hayMismaUnidad) {
                continue;                                  // otra unidad: se da por cubierto
            }
            BigDecimal falta = n.cantidad.subtract(disponible);
            if (falta.signum() > 0) {
                faltantes.add(new Faltante(n.ingrediente, n.unidad, falta));
            }
        }
        return faltantes;
    }

    private static Short idUnidad(UnidadMedida unidad) {
        return unidad == null ? null : unidad.getId();
    }
}
