package es.safareyes.smartchef.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RecetasPorTipoPlato {
    private String tipoPlato;
    private Long recetas;
}
