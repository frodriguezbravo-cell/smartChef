package es.safareyes.smartchef.controladores.dto;

import java.util.List;

public record ListaDetalleDto(
        Integer id,
        String nombre,
        Boolean completada,
        List<LineaListaDto> lineas) {
}
