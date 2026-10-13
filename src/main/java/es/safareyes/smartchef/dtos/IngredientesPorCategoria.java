package es.safareyes.smartchef.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class IngredientesPorCategoria {
    private String categoria;
    private Long ingredientes;
}
