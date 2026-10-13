package es.safareyes.smartchef.controladores.dto;

public record ListaDto(
        Integer id,
        String nombre,
        Boolean completada,
        Integer totalLineas,
        Integer pendientes) {
}
