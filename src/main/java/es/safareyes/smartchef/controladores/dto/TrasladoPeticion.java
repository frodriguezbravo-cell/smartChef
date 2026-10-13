package es.safareyes.smartchef.controladores.dto;

import jakarta.validation.constraints.NotNull;

public record TrasladoPeticion(
        @NotNull Integer idDespensa) {
}
