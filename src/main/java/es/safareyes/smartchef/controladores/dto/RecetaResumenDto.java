package es.safareyes.smartchef.controladores.dto;

import java.util.List;

public record RecetaResumenDto(
        Integer id,
        String titulo,
        String slug,
        String imagenUrl,
        String tipoPlato,
        String tipoCocina,
        String dificultad,
        Integer tiempoTotalMin,
        Short raciones,
        List<String> dietas) {
}
