package es.safareyes.smartchef.dtos;

import java.math.BigDecimal;

/**
 * Nutrición POR RACIÓN de una receta (viene de la vista v_receta_nutricion).
 * Es un interface: Spring crea el objeto a partir de los alias de la consulta nativa.
 */
public interface NutricionReceta {
    Integer getIdReceta();

    String getTitulo();

    Integer getRaciones();

    BigDecimal getCaloriasKcal();

    BigDecimal getProteinasG();

    BigDecimal getHidratosG();

    BigDecimal getAzucaresG();

    BigDecimal getGrasasG();

    BigDecimal getGrasasSaturadasG();

    BigDecimal getFibraG();

    BigDecimal getSalG();
}
