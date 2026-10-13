package es.safareyes.smartchef.controladores.dto;

import jakarta.validation.constraints.Size;

/** gravedad: INTOLERANCIA, ALERGIA_LEVE o ALERGIA_GRAVE (opcional, por defecto ALERGIA_LEVE). */
public record AlergiaPeticion(
        String gravedad,
        @Size(max = 255) String notas) {
}
