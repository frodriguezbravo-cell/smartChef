package es.safareyes.smartchef.repositorios;

import es.safareyes.smartchef.modelos.Articulo;
import es.safareyes.smartchef.pruebas.PruebaBaseDatos;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Artículos divulgativos y glosario de términos de cocina. */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ContenidoRepositoriesTest extends PruebaBaseDatos {

    @Autowired private IArticuloRepository articulos;
    @Autowired private IGlosarioTerminoRepository glosario;

    private int articulo(String slug, String titulo, boolean publicado, int diasAtras) {
        return bd().insertar("articulo", "INSERT INTO articulo (titulo, slug, id_categoria, contenido_md, publicado, publicado_en) VALUES (?1, ?2, "
                + "(SELECT id FROM cat_categoria_articulo WHERE codigo = 'TECNICAS'), '# Texto', " + (publicado ? "TRUE" : "FALSE")
                + ", now() - interval '" + diasAtras + " day')", titulo, slug);
    }

    @Test
    void buscaUnArticuloPorSuSlug() {
        articulo("como-hacer-un-sofrito", "Cómo hacer un sofrito", true, 1);
        Articulo encontrado = articulos.findBySlug("como-hacer-un-sofrito").orElseThrow();
        assertEquals("Cómo hacer un sofrito", encontrado.getTitulo());
        assertEquals("TECNICAS", encontrado.getCategoria().getCodigo());
        assertTrue(encontrado.getPublicado());
        assertTrue(articulos.findBySlug("no-existe").isEmpty());
    }

    @Test
    void losArticulosPublicadosVanDelMasRecienteAlMasAntiguo() {
        articulo("antiguo", "Antiguo", true, 5);
        articulo("borrador", "Borrador", false, 0);
        articulo("reciente", "Reciente", true, 1);
        articulo("intermedio", "Intermedio", true, 3);

        // la consulta devuelve todos los artículos publicados: nos quedamos con los de este test
        List<String> nuestros = List.of("antiguo", "borrador", "reciente", "intermedio");
        List<Articulo> resultado = articulos.findByPublicadoTrueOrderByPublicadoEnDesc().stream()
                .filter(a -> nuestros.contains(a.getSlug())).toList();
        assertEquals(3, resultado.size());                                       // el borrador no sale
        assertEquals("reciente", resultado.get(0).getSlug());
        assertEquals("intermedio", resultado.get(1).getSlug());
        assertEquals("antiguo", resultado.get(2).getSlug());
    }

    @Test
    void sinArticulosPublicadosLaListaEstaVacia() {
        articulo("solo-borrador", "Solo borrador", false, 0);
        assertTrue(articulos.findByPublicadoTrueOrderByPublicadoEnDesc().stream().noneMatch(a -> a.getSlug().equals("solo-borrador")));
    }

    @Test
    void elGlosarioSeBuscaPorParteDelTermino() {
        assertEquals(5, glosario.findAll().size());
        assertEquals(1, glosario.findByTerminoContainingIgnoreCase("SOFRITO").size());
        assertEquals("Al dente", glosario.findByTerminoContainingIgnoreCase("al dente").get(0).getTermino());
        assertTrue(glosario.findByTerminoContainingIgnoreCase("Mise").get(0).getDefinicion().contains("ingredientes"));
        assertTrue(glosario.findByTerminoContainingIgnoreCase("zzz").isEmpty());
    }
}
