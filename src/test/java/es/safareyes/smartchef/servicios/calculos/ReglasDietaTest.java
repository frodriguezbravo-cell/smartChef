package es.safareyes.smartchef.servicios.calculos;

import es.safareyes.smartchef.modelos.Receta;
import es.safareyes.smartchef.pruebas.Datos;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReglasDietaTest {

    @Test
    void sinDietasTodasLasRecetasValen() {
        Receta receta = Datos.receta("Pollo", 2);
        assertTrue(ReglasDieta.recetaCumple(receta, List.of()));
    }

    @Test
    void omnivoraNoRestringeNada() {
        Receta receta = Datos.receta("Pollo", 2);
        assertTrue(ReglasDieta.recetaCumple(receta, List.of("OMNIVORA")));
    }

    @Test
    void recetaVeganaValeParaVegetarianos() {
        Receta receta = Datos.receta("Ensalada", 2, "VEGANA");
        assertTrue(ReglasDieta.recetaCumple(receta, List.of("VEGETARIANA")));
    }

    @Test
    void recetaVegetarianaNoValeParaVeganos() {
        Receta receta = Datos.receta("Tortilla", 2, "VEGETARIANA");
        assertFalse(ReglasDieta.recetaCumple(receta, List.of("VEGANA")));
    }

    @Test
    void pescetarianosAceptanRecetasVegetarianas() {
        Receta receta = Datos.receta("Tortilla", 2, "VEGETARIANA");
        assertTrue(ReglasDieta.recetaCumple(receta, List.of("PESCETARIANA")));
    }

    @Test
    void sinGlutenExigeLaMarcaEnLaReceta() {
        Receta conMarca = Datos.receta("Arroz", 2, "SIN_GLUTEN");
        Receta sinMarca = Datos.receta("Pasta", 2, "VEGETARIANA");
        assertTrue(ReglasDieta.recetaCumple(conMarca, List.of("SIN_GLUTEN")));
        assertFalse(ReglasDieta.recetaCumple(sinMarca, List.of("SIN_GLUTEN")));
    }

    @Test
    void hayQueCumplirTodasLasDietasDelUsuario() {
        Receta receta = Datos.receta("Ensalada", 2, "VEGANA", "SIN_GLUTEN");
        assertTrue(ReglasDieta.recetaCumple(receta, List.of("VEGANA", "SIN_GLUTEN")));
        assertFalse(ReglasDieta.recetaCumple(receta, List.of("VEGANA", "SIN_LACTOSA")));
    }
}
