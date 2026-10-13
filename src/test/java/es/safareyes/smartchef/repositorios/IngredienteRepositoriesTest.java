package es.safareyes.smartchef.repositorios;

import es.safareyes.smartchef.dtos.IngredientesPorCategoria;
import es.safareyes.smartchef.modelos.Ingrediente;
import es.safareyes.smartchef.modelos.IngredienteImagen;
import es.safareyes.smartchef.pruebas.PruebaBaseDatos;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Ingredientes, sus categorías, imágenes y nombres alternativos (con los 14 ingredientes del script SQL). */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class IngredienteRepositoriesTest extends PruebaBaseDatos {

    @Autowired private IIngredienteRepository ingredientes;
    @Autowired private ICategoriaIngredienteRepository categorias;
    @Autowired private IIngredienteImagenRepository imagenes;
    @Autowired private IIngredienteNombreAlternativoRepository nombresAlternativos;

    @Test
    void buscaUnIngredientePorSuSlug() {
        Ingrediente tomate = ingredientes.findBySlug("tomate").orElseThrow();
        assertEquals("Tomate", tomate.getNombre());
        assertEquals("Verduras y hortalizas", tomate.getCategoria().getNombre());
        assertTrue(tomate.getAptoVegano());
        assertEquals(0, new java.math.BigDecimal("18").compareTo(tomate.getCaloriasKcal()));
        assertTrue(ingredientes.findBySlug("no-existe").isEmpty());
    }

    @Test
    void buscaPorParteDelNombreSinImportarMayusculas() {
        List<Ingrediente> aceites = ingredientes.findByNombreContainingIgnoreCase("ACEITE");
        assertEquals(1, aceites.size());
        assertEquals("Aceite de oliva virgen extra", aceites.get(0).getNombre());
        assertTrue(ingredientes.findByNombreContainingIgnoreCase("zzz").isEmpty());
    }

    @Test
    void filtraPorCategoriaSoloLosPublicados() {
        int lacteos = bd().idPor("categoria_ingrediente", "nombre", "Lácteos y huevos");
        List<Ingrediente> resultado = ingredientes.findByCategoriaIdAndPublicadaTrue(lacteos);
        assertEquals(3, resultado.size());                                   // huevo, leche y parmesano
        assertTrue(resultado.stream().anyMatch(i -> i.getNombre().equals("Queso parmesano")));
        assertTrue(ingredientes.findByCategoriaIdAndPublicadaTrue(lacteos + 1000).isEmpty());

        bd().ejecutar("UPDATE ingrediente SET publicada = FALSE WHERE nombre = 'Huevo'");
        assertEquals(2, ingredientes.findByCategoriaIdAndPublicadaTrue(lacteos).size());
    }

    @Test
    void losIngredientesVeganosSonLosAptosYPublicados() {
        List<Ingrediente> veganos = ingredientes.findByAptoVeganoTrueAndPublicadaTrue();
        assertEquals(9, veganos.size());
        assertTrue(veganos.stream().noneMatch(i -> i.getNombre().equals("Huevo")));
        assertTrue(veganos.stream().anyMatch(i -> i.getNombre().equals("Garbanzos cocidos")));
    }

    @Test
    void excluyeLosIngredientesConLosAlergenosIndicados() {
        List<Ingrediente> sinGluten = ingredientes.buscarSinAlergenos(List.of("GLUTEN"));
        assertEquals(12, sinGluten.size());                                  // 14 menos los espaguetis y la harina
        assertTrue(sinGluten.stream().noneMatch(i -> i.getNombre().equals("Espaguetis")));
        assertTrue(sinGluten.stream().anyMatch(i -> i.getNombre().equals("Arroz blanco")));

        assertEquals(12, ingredientes.buscarSinAlergenos(List.of("LACTEOS")).size());     // sin leche ni parmesano
        assertEquals(14, ingredientes.buscarSinAlergenos(List.of("SOJA")).size());        // ninguno lleva soja
        assertEquals(7, ingredientes.buscarSinAlergenos(
                List.of("GLUTEN", "LACTEOS", "PESCADO", "HUEVOS", "FRUTOS_CASCARA")).size());
    }

    @Test
    void losIngredientesNoPublicadosNuncaSeOfrecen() {
        bd().ejecutar("UPDATE ingrediente SET publicada = FALSE WHERE nombre = 'Tomate'");
        assertTrue(ingredientes.buscarSinAlergenos(List.of("SOJA")).stream().noneMatch(i -> i.getNombre().equals("Tomate")));
        assertEquals(13, ingredientes.buscarSinAlergenos(List.of("SOJA")).size());
    }

    @Test
    void cuentaLosIngredientesDeCadaCategoria() {
        List<IngredientesPorCategoria> informe = categorias.contarIngredientesPorCategoria();
        assertEquals(9, informe.size());                                     // "Frutas" no tiene ingredientes y no sale
        assertEquals("Verduras y hortalizas", informe.get(0).getCategoria());  // ordenado por el orden de la categoría
        assertEquals(2, informe.get(0).getIngredientes());
        assertEquals(3, informe.stream().filter(i -> i.getCategoria().equals("Lácteos y huevos")).findFirst().orElseThrow().getIngredientes());
        assertEquals(14, informe.stream().mapToLong(IngredientesPorCategoria::getIngredientes).sum());
        assertTrue(informe.stream().noneMatch(i -> i.getCategoria().equals("Frutas")));
    }

    @Test
    void lasImagenesSeDevuelvenOrdenadas() {
        int idTomate = bd().idPor("ingrediente", "nombre", "Tomate");
        bd().ejecutar("INSERT INTO ingrediente_imagen (id_ingrediente, url, es_principal, orden) VALUES (?1, 'http://x/segunda.jpg', FALSE, 2)", idTomate);
        bd().ejecutar("INSERT INTO ingrediente_imagen (id_ingrediente, url, es_principal, orden) VALUES (?1, 'http://x/primera.jpg', TRUE, 1)", idTomate);

        List<IngredienteImagen> resultado = imagenes.findByIngredienteIdOrderByOrdenAsc(idTomate);
        assertEquals(2, resultado.size());
        assertEquals("http://x/primera.jpg", resultado.get(0).getUrl());
        assertTrue(resultado.get(0).getEsPrincipal());
        assertEquals("http://x/segunda.jpg", resultado.get(1).getUrl());
        assertTrue(imagenes.findByIngredienteIdOrderByOrdenAsc(bd().idPor("ingrediente", "nombre", "Ajo")).isEmpty());
    }

    @Test
    void buscaPorNombresAlternativos() {
        assertEquals(2, nombresAlternativos.findByNombreContainingIgnoreCase("TOMAT").size());    // Jitomate y Tomato
        assertEquals("Tomate", nombresAlternativos.findByNombreContainingIgnoreCase("jitomate").get(0).getIngrediente().getNombre());
        assertEquals("es", nombresAlternativos.findByNombreContainingIgnoreCase("jitomate").get(0).getIdioma());
        assertEquals("Garbanzos cocidos", nombresAlternativos.findByNombreContainingIgnoreCase("chick").get(0).getIngrediente().getNombre());
        assertTrue(nombresAlternativos.findByNombreContainingIgnoreCase("zzz").isEmpty());
    }
}
