package es.safareyes.smartchef.controladores.dto;

import java.math.BigDecimal;

/** descripcion es el nombre del ingrediente o, si es una línea libre, el texto escrito por el usuario. */
public record LineaListaDto(
        Integer id,
        Integer idIngrediente,
        String descripcion,
        BigDecimal cantidad,
        String unidad,
        String receta,
        Boolean comprado) {
}
