package es.safareyes.smartchef.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/** Una línea de ingredientes de una receta, con la cantidad ya ajustada al número de raciones. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IngredienteEscalado {
    private Integer idIngrediente;
    private String nombre;
    private BigDecimal cantidad;          // null si es "al gusto"
    private BigDecimal cantidadGramos;
    private String unidadCodigo;
    private String unidadNombre;
    private String grupo;
    private String notas;
    private Boolean opcional;
}
