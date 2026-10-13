package es.safareyes.smartchef.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Nutrición de una receta para un número de raciones.
 * "completo" es false si algún ingrediente obligatorio no tiene cantidad en gramos o calorías:
 * en ese caso los valores son una estimación a la baja (lineasSinDatos dice cuántos faltan).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResumenNutricional {
    private BigDecimal caloriasKcal;
    private BigDecimal proteinasG;
    private BigDecimal hidratosG;
    private BigDecimal azucaresG;
    private BigDecimal grasasG;
    private BigDecimal grasasSaturadasG;
    private BigDecimal fibraG;
    private BigDecimal salG;
    private Boolean completo;
    private Integer lineasSinDatos;
}
