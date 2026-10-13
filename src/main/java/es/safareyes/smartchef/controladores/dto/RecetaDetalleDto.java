package es.safareyes.smartchef.controladores.dto;

import es.safareyes.smartchef.dtos.IngredienteEscalado;
import es.safareyes.smartchef.dtos.ResumenNutricional;
import java.util.List;

/** Receta con ingredientes y nutrición ajustados a "racionesSolicitadas". */
public record RecetaDetalleDto(
        Integer id,
        String titulo,
        String slug,
        String descripcion,
        String imagenUrl,
        String tipoPlato,
        String tipoCocina,
        String dificultad,
        Short tiempoPreparacionMin,
        Short tiempoCoccionMin,
        Short racionesReceta,
        Integer racionesSolicitadas,
        List<String> dietas,
        List<String> alergenos,
        List<IngredienteEscalado> ingredientes,
        List<PasoDto> pasos,
        ResumenNutricional nutricion) {
}
