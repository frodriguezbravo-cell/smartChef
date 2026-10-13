package es.safareyes.smartchef.controladores.dto;

import jakarta.validation.constraints.NotBlank;

/** motivo: CONSUMIDO, CADUCADO, DESECHADO, REGALADO u OTRO. */
public record BajaProductoPeticion(
        @NotBlank String motivo) {
}
