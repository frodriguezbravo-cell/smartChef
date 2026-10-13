package es.safareyes.smartchef.repositorios;

import es.safareyes.smartchef.dtos.NutricionReceta;
import es.safareyes.smartchef.dtos.RecetasPorTipoPlato;
import es.safareyes.smartchef.modelos.Receta;
import es.safareyes.smartchef.modelos.RecetaImagen;
import es.safareyes.smartchef.modelos.RecetaIngrediente;
import es.safareyes.smartchef.modelos.RecetaPaso;
import es.safareyes.smartchef.pruebas.PruebaBaseDatos;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Las consultas de recetas contra las 3 recetas del script SQL:
 * espaguetis-al-pomodoro (30 min, fácil, vegetariana, con gluten y lácteos),
 * pollo-al-ajillo-con-arroz (45 min, media, sin gluten) y ensalada-de-garbanzos (10 min, fácil, vegana).
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class RecetaRepositoriesTest extends PruebaBaseDatos {

    private static final String ESPAGUETIS = "espaguetis-al-pomodoro";
    private static final String POLLO = "pollo-al-ajillo-con-arroz";
    private static final String ENSALADA = "ensalada-de-garbanzos";

    @Autowired private IRecetaRepository recetas;
    @Autowired private IRecetaIngredienteRepository lineas;
    @Autowired private IRecetaPasoRepository pasos;
    @Autowired private IRecetaImagenRepository imagenes;

    private static List<String> slugs(List<Receta> lista) {
        return lista.stream().map(Receta::getSlug).sorted().toList();
    }

    private static void assertNumero(String esperado, BigDecimal real) {
        assertNotNull(real);
        assertTrue(new BigDecimal(esperado).compareTo(real) == 0, "Se esperaba " + esperado + " pero fue " + real);
    }

    @Test
    void buscaUnaRecetaPorSlug() {
        Receta receta = recetas.findBySlug(ESPAGUETIS).orElseThrow();
        assertEquals("Espaguetis al pomodoro", receta.getTitulo());
        assertEquals("Plato principal", receta.getTipoPlato().getNombre());
        assertEquals("ITALIANA", receta.getTipoCocina().getCodigo());
        assertEquals("FACIL", receta.getDificultad().getCodigo());
        assertEquals("Italia", receta.getOrigen().getNombre());
        assertEquals(2, receta.getRaciones().intValue());
        assertTrue(recetas.findBySlug("no-existe").isEmpty());
    }

    @Test
    void listaLasRecetasPublicadas() {
        assertEquals(3, recetas.findByPublicadaTrue().size());
        bd().ejecutar("UPDATE receta SET publicada = FALSE WHERE slug = ?1", POLLO);
        assertEquals(List.of(ENSALADA, ESPAGUETIS), slugs(recetas.findByPublicadaTrue()));
    }

    @Test
    void buscaPorTituloSoloEntreLasPublicadas() {
        assertEquals(List.of(POLLO), slugs(recetas.findByTituloContainingIgnoreCaseAndPublicadaTrue("AJILLO")));
        assertTrue(recetas.findByTituloContainingIgnoreCaseAndPublicadaTrue("zzz").isEmpty());
        bd().ejecutar("UPDATE receta SET publicada = FALSE WHERE slug = ?1", POLLO);
        assertTrue(recetas.findByTituloContainingIgnoreCaseAndPublicadaTrue("ajillo").isEmpty());
    }

    @Test
    void filtraPorDificultad() {
        assertEquals(List.of(ENSALADA, ESPAGUETIS), slugs(recetas.findByDificultadCodigoAndPublicadaTrue("FACIL")));
        assertEquals(List.of(POLLO), slugs(recetas.findByDificultadCodigoAndPublicadaTrue("MEDIA")));
        assertTrue(recetas.findByDificultadCodigoAndPublicadaTrue("DIFICIL").isEmpty());
    }

    @Test
    void lasRecetasRapidasSumanPreparacionYCoccion() {
        assertTrue(recetas.buscarRapidas(9).isEmpty());
        assertEquals(List.of(ENSALADA), slugs(recetas.buscarRapidas(10)));                  // 10 + 0
        assertEquals(List.of(ENSALADA, ESPAGUETIS), slugs(recetas.buscarRapidas(30)));      // espaguetis: 10 + 20
        assertEquals(List.of(ENSALADA, ESPAGUETIS, POLLO), slugs(recetas.buscarRapidas(45)));   // pollo: 15 + 30
    }

    @Test
    void losTiemposNulosCuentanComoCero() {
        bd().ejecutar("UPDATE receta SET tiempo_coccion_min = NULL, tiempo_preparacion_min = NULL WHERE slug = ?1", POLLO);
        assertEquals(List.of(POLLO), slugs(recetas.buscarRapidas(0)));          // 0 + 0: es la única sin tiempos
    }

    @Test
    void filtraPorDietaApta() {
        assertEquals(List.of(ENSALADA, ESPAGUETIS), slugs(recetas.buscarPorDieta("VEGETARIANA")));
        assertEquals(List.of(ENSALADA), slugs(recetas.buscarPorDieta("VEGANA")));
        assertEquals(List.of(ENSALADA, POLLO), slugs(recetas.buscarPorDieta("SIN_GLUTEN")));
        assertEquals(List.of(ENSALADA, POLLO), slugs(recetas.buscarPorDieta("SIN_LACTOSA")));
        assertEquals(List.of(POLLO), slugs(recetas.buscarPorDieta("ALTA_PROTEINA")));
        assertTrue(recetas.buscarPorDieta("KETO").isEmpty());
    }

    @Test
    void excluyeLasRecetasConAlergenos() {
        assertEquals(List.of(ENSALADA, POLLO), slugs(recetas.buscarSinAlergenos(List.of("GLUTEN"))));
        assertEquals(List.of(ENSALADA, POLLO), slugs(recetas.buscarSinAlergenos(List.of("LACTEOS"))));   // el parmesano es opcional pero cuenta
        assertEquals(List.of(ENSALADA, POLLO), slugs(recetas.buscarSinAlergenos(List.of("GLUTEN", "LACTEOS"))));
        assertEquals(List.of(ENSALADA, ESPAGUETIS, POLLO), slugs(recetas.buscarSinAlergenos(List.of("PESCADO"))));
        assertEquals(List.of(ENSALADA, ESPAGUETIS, POLLO), slugs(recetas.buscarSinAlergenos(List.of("HUEVOS", "SOJA"))));
    }

    @Test
    void lasRecetasNoPublicadasNoSeOfrecenComoSeguras() {
        bd().ejecutar("UPDATE receta SET publicada = FALSE WHERE slug = ?1", POLLO);
        assertEquals(List.of(ENSALADA), slugs(recetas.buscarSinAlergenos(List.of("GLUTEN"))));
    }

    @Test
    void cocinablesSonLasQueTienenTodoLoObligatorioEnLaDespensa() {
        int usuario = bd().usuario("cocinero");
        int despensa = bd().despensa(usuario, "Casa");
        assertTrue(recetas.buscarCocinablesConDespensa(despensa).isEmpty());                       // despensa vacía

        for (String ingrediente : List.of("Espaguetis", "Tomate", "Ajo", "Aceite de oliva virgen extra")) {
            bd().producto(despensa, ingrediente, null, false);
        }
        assertEquals(List.of(ESPAGUETIS), slugs(recetas.buscarCocinablesConDespensa(despensa)));   // sin parmesano: es opcional

        bd().producto(despensa, "Garbanzos cocidos", null, false);
        bd().producto(despensa, "Cebolla", null, false);
        assertEquals(List.of(ENSALADA, ESPAGUETIS), slugs(recetas.buscarCocinablesConDespensa(despensa)));
        assertTrue(recetas.buscarCocinablesConDespensa(despensa + 1000).isEmpty());
    }

    @Test
    void losProductosDadosDeBajaNoCuentanParaCocinar() {
        int usuario = bd().usuario("cocinera");
        int despensa = bd().despensa(usuario, "Casa");
        for (String ingrediente : List.of("Espaguetis", "Ajo", "Aceite de oliva virgen extra")) {
            bd().producto(despensa, ingrediente, null, false);
        }
        bd().producto(despensa, "Tomate", null, true);                                              // el tomate ya se ha consumido
        assertTrue(recetas.buscarCocinablesConDespensa(despensa).isEmpty());

        bd().producto(despensa, "Tomate", null, false);                                             // vuelve a haber tomate
        assertEquals(List.of(ESPAGUETIS), slugs(recetas.buscarCocinablesConDespensa(despensa)));
    }

    @Test
    void cuentaLasRecetasDeCadaTipoDePlato() {
        List<RecetasPorTipoPlato> informe = recetas.contarRecetasPorTipoPlato();
        assertEquals(2, informe.size());
        assertEquals("Ensalada", informe.get(0).getTipoPlato());                   // ordenado por el orden del tipo de plato
        assertEquals(1, informe.get(0).getRecetas());
        assertEquals("Plato principal", informe.get(1).getTipoPlato());
        assertEquals(2, informe.get(1).getRecetas());

        bd().ejecutar("UPDATE receta SET publicada = FALSE WHERE slug = ?1", POLLO);
        assertEquals(1, recetas.contarRecetasPorTipoPlato().get(1).getRecetas());
    }

    @Test
    void calculaLaNutricionPorRacionDesdeLaVista() {
        NutricionReceta espaguetis = recetas.nutricionPorRacion(bd().idPor("receta", "slug", ESPAGUETIS)).orElseThrow();
        assertEquals("Espaguetis al pomodoro", espaguetis.getTitulo());
        assertEquals(2, espaguetis.getRaciones());
        assertNumero("480.5", espaguetis.getCaloriasKcal());
        assertNumero("14.6", espaguetis.getProteinasG());
        assertNumero("81.1", espaguetis.getHidratosG());
        assertNumero("10.9", espaguetis.getGrasasG());
        assertNumero("0.03", espaguetis.getSalG());

        NutricionReceta pollo = recetas.nutricionPorRacion(bd().idPor("receta", "slug", POLLO)).orElseThrow();
        assertNumero("709.1", pollo.getCaloriasKcal());
        assertNumero("50.8", pollo.getProteinasG());
        assertNumero("63.4", pollo.getHidratosG());
        assertNumero("26.2", pollo.getGrasasG());
        assertNumero("0.27", pollo.getSalG());

        NutricionReceta ensalada = recetas.nutricionPorRacion(bd().idPor("receta", "slug", ENSALADA)).orElseThrow();
        assertNumero("489.8", ensalada.getCaloriasKcal());
        assertNumero("19.5", ensalada.getProteinasG());
        assertNumero("64.3", ensalada.getHidratosG());
        assertNumero("19.0", ensalada.getGrasasG());
    }

    @Test
    void sinIngredientesConGramosNoHayNutricion() {
        int vacia = bd().insertar("receta", "INSERT INTO receta (titulo, slug, id_tipo_plato) VALUES ('Receta vacía', 'receta-vacia', "
                + "(SELECT id FROM cat_tipo_plato WHERE codigo = 'POSTRE'))");
        assertTrue(recetas.nutricionPorRacion(vacia).isEmpty());
        assertTrue(recetas.nutricionPorRacion(vacia + 1000).isEmpty());
    }

    @Test
    void laNutricionIgnoraLosIngredientesOpcionales() {
        // los espaguetis llevan 20 g de parmesano opcional (78,4 kcal): si contara, las calorías subirían
        int idReceta = bd().idPor("receta", "slug", ESPAGUETIS);
        assertNumero("480.5", recetas.nutricionPorRacion(idReceta).orElseThrow().getCaloriasKcal());
        bd().ejecutar("UPDATE receta_ingrediente SET es_opcional = FALSE WHERE id_receta = ?1 AND id_ingrediente = "
                + "(SELECT id FROM ingrediente WHERE nombre = 'Queso parmesano')", idReceta);
        assertNumero("519.7", recetas.nutricionPorRacion(idReceta).orElseThrow().getCaloriasKcal());   // + 78,4 / 2 = + 39,2
    }

    @Test
    void losIngredientesDeUnaRecetaSeDevuelvenEnSuOrden() {
        List<RecetaIngrediente> resultado = lineas.findByRecetaIdOrderByOrdenAsc(bd().idPor("receta", "slug", ESPAGUETIS));
        assertEquals(5, resultado.size());
        assertEquals("Espaguetis", resultado.get(0).getIngrediente().getNombre());
        assertEquals("Tomate", resultado.get(1).getIngrediente().getNombre());
        assertEquals("Queso parmesano", resultado.get(4).getIngrediente().getNombre());
        assertTrue(resultado.get(4).getEsOpcional());
        assertFalse(resultado.get(0).getEsOpcional());
        assertEquals("gramos (g)", resultado.get(0).getUnidad().getNombre());
        assertEquals("Para la salsa", resultado.get(1).getGrupo());
        assertTrue(lineas.findByRecetaIdOrderByOrdenAsc(bd().idPor("receta", "slug", ESPAGUETIS) + 1000).isEmpty());
    }

    @Test
    void losPasosSeDevuelvenNumerados() {
        List<RecetaPaso> resultado = pasos.findByRecetaIdOrderByNumeroAsc(bd().idPor("receta", "slug", ESPAGUETIS));
        assertEquals(3, resultado.size());
        assertEquals(1, resultado.get(0).getNumero().intValue());
        assertEquals(3, resultado.get(2).getNumero().intValue());
        assertEquals(2, pasos.findByRecetaIdOrderByNumeroAsc(bd().idPor("receta", "slug", ENSALADA)).size());
        assertEquals(10, pasos.findByRecetaIdOrderByNumeroAsc(bd().idPor("receta", "slug", ENSALADA)).get(1).getDuracionMin().intValue());
    }

    @Test
    void lasImagenesDeUnaRecetaVanOrdenadas() {
        int id = bd().idPor("receta", "slug", POLLO);
        bd().ejecutar("INSERT INTO receta_imagen (id_receta, url, es_principal, orden) VALUES (?1, 'http://x/b.jpg', FALSE, 2)", id);
        bd().ejecutar("INSERT INTO receta_imagen (id_receta, url, es_principal, orden) VALUES (?1, 'http://x/a.jpg', TRUE, 1)", id);
        List<RecetaImagen> resultado = imagenes.findByRecetaIdOrderByOrdenAsc(id);
        assertEquals(2, resultado.size());
        assertEquals("http://x/a.jpg", resultado.get(0).getUrl());
        assertTrue(imagenes.findByRecetaIdOrderByOrdenAsc(bd().idPor("receta", "slug", ESPAGUETIS)).isEmpty());
    }
}
