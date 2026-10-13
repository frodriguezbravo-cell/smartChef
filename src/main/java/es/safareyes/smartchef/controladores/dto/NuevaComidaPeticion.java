package es.safareyes.smartchef.controladores.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalTime;

/** tipoComida: DESAYUNO, ALMUERZO, COMIDA, MERIENDA o CENA. Hace falta idReceta o descripcion. horaAviso activa el aviso. */
public record NuevaComidaPeticion(
        @NotNull LocalDate fecha,
        @NotBlank String tipoComida,
        Integer idReceta,
        @Size(max = 150) String descripcion,
        @NotNull @Positive @Max(100) Integer raciones,
        LocalTime horaAviso) {
}
