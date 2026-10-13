package es.safareyes.smartchef.servicios.calculos;

import es.safareyes.smartchef.dtos.IngredienteEscalado;
import es.safareyes.smartchef.modelos.Receta;
import es.safareyes.smartchef.modelos.RecetaIngrediente;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/** Ajusta las cantidades de una receta a otro número de raciones. */
public final class Escalador {

    private Escalador() {
    }

    /** raciones deseadas / raciones de la receta (por ejemplo, 3 / 2 = 1,5). */
    public static BigDecimal factor(Receta receta, int racionesDeseadas) {
        if (racionesDeseadas <= 0) {
            throw new IllegalArgumentException("Las raciones deben ser mayores que 0");
        }
        if (receta.getRaciones() == null || receta.getRaciones() <= 0) {
            throw new IllegalArgumentException("La receta no tiene un número de raciones válido");
        }
        return BigDecimal.valueOf(racionesDeseadas)
                .divide(BigDecimal.valueOf(receta.getRaciones()), 6, RoundingMode.HALF_UP);
    }

    public static List<IngredienteEscalado> escalar(Receta receta, int racionesDeseadas) {
        BigDecimal factor = factor(receta, racionesDeseadas);
        List<IngredienteEscalado> resultado = new ArrayList<>();
        for (RecetaIngrediente linea : receta.getIngredientes()) {
            IngredienteEscalado e = new IngredienteEscalado();
            e.setIdIngrediente(linea.getIngrediente().getId());
            e.setNombre(linea.getIngrediente().getNombre());
            e.setCantidad(escalarValor(linea.getCantidad(), factor));
            e.setCantidadGramos(escalarValor(linea.getCantidadGramos(), factor));
            if (linea.getUnidad() != null) {
                e.setUnidadCodigo(linea.getUnidad().getCodigo());
                e.setUnidadNombre(linea.getUnidad().getNombre());
            }
            e.setGrupo(linea.getGrupo());
            e.setNotas(linea.getNotas());
            e.setOpcional(Boolean.TRUE.equals(linea.getEsOpcional()));
            resultado.add(e);
        }
        return resultado;
    }

    private static BigDecimal escalarValor(BigDecimal valor, BigDecimal factor) {
        return valor == null ? null : valor.multiply(factor).setScale(2, RoundingMode.HALF_UP);
    }
}
