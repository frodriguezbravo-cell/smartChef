package es.safareyes.smartchef.servicios.calculos;

import es.safareyes.smartchef.modelos.Receta;
import es.safareyes.smartchef.modelos.catalogos.Dieta;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Reglas para saber si una receta encaja con las dietas de un usuario.
 * <ul>
 *   <li>OMNIVORA no restringe nada.</li>
 *   <li>Una receta VEGANA también vale para quien sigue una dieta VEGETARIANA.</li>
 *   <li>Una receta VEGETARIANA o VEGANA también vale para quien sigue una dieta PESCETARIANA.</li>
 *   <li>Para el resto (SIN_GLUTEN, SIN_LACTOSA, KETO...) la receta debe estar marcada con esa dieta.</li>
 * </ul>
 * El usuario debe cumplirlas TODAS.
 */
public final class ReglasDieta {

    private ReglasDieta() {
    }

    public static boolean recetaCumple(Receta receta, Collection<String> dietasDelUsuario) {
        Set<String> dietasReceta = receta.getDietas().stream()
                .map(Dieta::getCodigo)
                .collect(Collectors.toSet());
        for (String dieta : dietasDelUsuario) {
            if (!cumple(dietasReceta, dieta)) {
                return false;
            }
        }
        return true;
    }

    private static boolean cumple(Set<String> dietasReceta, String dietaUsuario) {
        return switch (dietaUsuario) {
            case "OMNIVORA" -> true;
            case "VEGETARIANA" -> dietasReceta.contains("VEGETARIANA") || dietasReceta.contains("VEGANA");
            case "PESCETARIANA" -> dietasReceta.contains("PESCETARIANA")
                    || dietasReceta.contains("VEGETARIANA") || dietasReceta.contains("VEGANA");
            default -> dietasReceta.contains(dietaUsuario);
        };
    }
}
