package es.safareyes.smartchef.controladores.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

/** unidad: G, KG, ML, L, UD...; ubicacion: ALACENA, NEVERA o CONGELADOR (opcional); fechaCaducidad opcional. */
public record NuevoProductoPeticion(
        @NotNull Integer idIngrediente,
        @NotNull @Positive BigDecimal cantidad,
        @NotBlank String unidad,
        String ubicacion,
        LocalDate fechaCaducidad) {
}
