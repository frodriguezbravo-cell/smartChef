package es.safareyes.smartchef.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Calorías planificadas para un día frente al objetivo del usuario.
 * diferencia = objetivo - planificadas (positiva: aún quedan calorías; negativa: se supera el objetivo).
 * comidasSinDatos cuenta las comidas libres o con ingredientes sin datos nutricionales.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResumenDia {
    private LocalDate fecha;
    private Integer totalComidas;
    private BigDecimal caloriasPlanificadas;
    private Integer comidasSinDatos;
    private Integer objetivoCalorias;
    private BigDecimal diferencia;       // null si el usuario no tiene objetivo
}
