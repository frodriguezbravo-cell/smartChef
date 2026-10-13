package es.safareyes.smartchef;

import es.safareyes.smartchef.dtos.NutricionReceta;
import es.safareyes.smartchef.modelos.Ingrediente;
import es.safareyes.smartchef.modelos.Receta;
import es.safareyes.smartchef.repositorios.IIngredienteRepository;
import es.safareyes.smartchef.repositorios.IRecetaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas sobre la base de datos real con los datos de ejemplo del script SQL.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class RepositoriesTest {

    @Autowired
    private IRecetaRepository recetaRepository;

    @Autowired
    private IIngredienteRepository ingredienteRepository;

    @Test
    void encuentraRecetaPorSlug() {
        Optional<Receta> receta = recetaRepository.findBySlug("espaguetis-al-pomodoro");
        assertTrue(receta.isPresent());
        assertEquals("Espaguetis al pomodoro", receta.get().getTitulo());
    }

    @Test
    void recetasSinGlutenNoIncluyenLaPasta() {
        List<Receta> sinGluten = recetaRepository.buscarSinAlergenos(List.of("GLUTEN"));
        assertFalse(sinGluten.stream().anyMatch(r -> r.getSlug().equals("espaguetis-al-pomodoro")));
        assertTrue(sinGluten.stream().anyMatch(r -> r.getSlug().equals("ensalada-de-garbanzos")));
    }

    @Test
    void recetasVeganas() {
        List<Receta> veganas = recetaRepository.buscarPorDieta("VEGANA");
        assertEquals(1, veganas.size());
    }

    @Test
    void ingredientesSinLactosa() {
        List<Ingrediente> sinLacteos = ingredienteRepository.buscarSinAlergenos(List.of("LACTEOS"));
        assertFalse(sinLacteos.stream().anyMatch(i -> i.getNombre().equals("Leche entera")));
        assertTrue(sinLacteos.stream().anyMatch(i -> i.getNombre().equals("Tomate")));
    }

    @Test
    void nutricionPorRacionDeLosEspaguetis() {
        Receta receta = recetaRepository.findBySlug("espaguetis-al-pomodoro").orElseThrow();
        NutricionReceta nutricion = recetaRepository.nutricionPorRacion(receta.getId()).orElseThrow();
        // (718 + 72 + 11,92 + 159,12) kcal / 2 raciones = 480,5 kcal (el parmesano es opcional y no cuenta)
        assertEquals(0, new BigDecimal("480.5").compareTo(nutricion.getCaloriasKcal()));
    }

    @Test
    void cocinablesConDespensaVaciaNoDevuelveNada() {
        assertTrue(recetaRepository.buscarCocinablesConDespensa(0).isEmpty());
    }
}
