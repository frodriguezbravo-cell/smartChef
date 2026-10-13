package es.safareyes.smartchef.controladores.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NuevaListaPeticion(
        @NotBlank @Size(max = 150) String nombre) {
}
