package es.safareyes.smartchef.controladores.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/** Códigos de dieta (VEGANA, SIN_GLUTEN...). Lista vacía = sin dietas. */
public record DietasPeticion(
        @NotNull List<@NotBlank String> dietas) {
}
