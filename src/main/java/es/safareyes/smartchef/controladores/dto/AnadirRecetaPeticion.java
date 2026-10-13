package es.safareyes.smartchef.controladores.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/** idDespensa (opcional): si se indica, solo se añade lo que NO hay en esa despensa. */
public record AnadirRecetaPeticion(
        @NotNull Integer idReceta,
        @NotNull @Positive @Max(100) Integer raciones,
        Integer idDespensa) {
}
