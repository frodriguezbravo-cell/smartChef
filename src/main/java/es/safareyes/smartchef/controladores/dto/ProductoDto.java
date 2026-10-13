package es.safareyes.smartchef.controladores.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/** diasParaCaducar es negativo si ya ha caducado y null si no tiene fecha de caducidad. */
public record ProductoDto(
        Integer id,
        Integer idIngrediente,
        String ingrediente,
        BigDecimal cantidad,
        String unidad,
        String ubicacion,
        LocalDate fechaCompra,
        LocalDate fechaCaducidad,
        Long diasParaCaducar,
        LocalDate fechaBaja,
        String motivoBaja) {
}
