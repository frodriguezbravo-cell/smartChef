package es.safareyes.smartchef.controladores.dto;

import jakarta.validation.constraints.NotNull;

public record CompradoPeticion(
        @NotNull Boolean comprado) {
}
