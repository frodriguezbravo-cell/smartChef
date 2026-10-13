package es.safareyes.smartchef.repositorios;

import es.safareyes.smartchef.modelos.catalogos.Continente;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Los 12 repositorios de catálogo: cada código del script SQL se encuentra y los inexistentes devuelven vacío. */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CatalogosRepositoriesTest {

    @Autowired private IAlergenoRepository alergenos;
    @Autowired private IDietaRepository dietas;
    @Autowired private IGravedadRepository gravedades;
    @Autowired private IListaGuardadaRepository listasGuardadas;
    @Autowired private IMotivoBajaRepository motivosBaja;
    @Autowired private IRolUsuarioRepository roles;
    @Autowired private ITipoComidaRepository tiposComida;
    @Autowired private IUbicacionDespensaRepository ubicaciones;
    @Autowired private IUnidadMedidaRepository unidades;
    @Autowired private IUnidadTemperaturaRepository temperaturas;
    @Autowired private IUnidadVolumenRepository volumenes;
    @Autowired private IContinenteRepository continentes;

    @Test
    void alergenos() {
        assertEquals(14, alergenos.findAll().size());                 // los 14 de la Unión Europea
        assertEquals(1, alergenos.findByCodigo("GLUTEN").orElseThrow().getOrden().intValue());
        assertEquals(7, alergenos.findByCodigo("LACTEOS").orElseThrow().getOrden().intValue());
        assertTrue(alergenos.findByCodigo("NO_EXISTE").isEmpty());
    }

    @Test
    void dietas() {
        assertEquals(9, dietas.findAll().size());
        assertEquals(3, dietas.findByCodigo("VEGANA").orElseThrow().getOrden().intValue());
        assertTrue(dietas.findByCodigo("VEGANA").orElseThrow().getActivo());
        assertTrue(dietas.findByCodigo("NO_EXISTE").isEmpty());
    }

    @Test
    void gravedades() {
        assertEquals(3, gravedades.findAll().size());
        assertEquals(3, gravedades.findByCodigo("ALERGIA_GRAVE").orElseThrow().getOrden().intValue());
        assertTrue(gravedades.findByCodigo("NO_EXISTE").isEmpty());
    }

    @Test
    void listasGuardadas() {
        assertEquals(2, listasGuardadas.findAll().size());
        assertEquals(1, listasGuardadas.findByCodigo("FAVORITA").orElseThrow().getOrden().intValue());
        assertEquals("PARA_COCINAR", listasGuardadas.findByCodigo("PARA_COCINAR").orElseThrow().getCodigo());
        assertTrue(listasGuardadas.findByCodigo("NO_EXISTE").isEmpty());
    }

    @Test
    void motivosDeBaja() {
        assertEquals(5, motivosBaja.findAll().size());
        assertEquals(1, motivosBaja.findByCodigo("CONSUMIDO").orElseThrow().getOrden().intValue());
        assertTrue(motivosBaja.findByCodigo("NO_EXISTE").isEmpty());
    }

    @Test
    void rolesDeUsuario() {
        assertEquals(3, roles.findAll().size());
        assertEquals(1, roles.findByCodigo("USUARIO").orElseThrow().getOrden().intValue());
        assertTrue(roles.findByCodigo("ADMIN").isPresent());
        assertTrue(roles.findByCodigo("NO_EXISTE").isEmpty());
    }

    @Test
    void tiposDeComida() {
        assertEquals(5, tiposComida.findAll().size());
        assertEquals(5, tiposComida.findByCodigo("CENA").orElseThrow().getOrden().intValue());
        assertTrue(tiposComida.findByCodigo("NO_EXISTE").isEmpty());
    }

    @Test
    void ubicacionesDeLaDespensa() {
        assertEquals(3, ubicaciones.findAll().size());
        assertEquals(2, ubicaciones.findByCodigo("NEVERA").orElseThrow().getOrden().intValue());
        assertTrue(ubicaciones.findByCodigo("NO_EXISTE").isEmpty());
    }

    @Test
    void unidadesDeMedida() {
        assertEquals(10, unidades.findAll().size());
        assertEquals(1, unidades.findByCodigo("G").orElseThrow().getOrden().intValue());
        assertTrue(unidades.findByCodigo("AL_GUSTO").isPresent());
        assertTrue(unidades.findByCodigo("NO_EXISTE").isEmpty());
    }

    @Test
    void unidadesDeTemperaturaYVolumen() {
        assertEquals(2, temperaturas.findAll().size());
        assertTrue(temperaturas.findByCodigo("C").isPresent());
        assertTrue(temperaturas.findByCodigo("F").isPresent());
        assertTrue(temperaturas.findByCodigo("K").isEmpty());
        assertEquals(2, volumenes.findAll().size());
        assertTrue(volumenes.findByCodigo("L").isPresent());
        assertTrue(volumenes.findByCodigo("GAL").isPresent());
        assertTrue(volumenes.findByCodigo("ML").isEmpty());
    }

    @Test
    void continentesPorNombreYOrden() {
        assertEquals(8, continentes.findAll().size());
        List<Continente> americas = continentes.findAllByNombreContainingAndOrdenIsLessThan("américa", (short) 4);
        assertEquals(3, americas.size());                            // Sudamérica, Centroamérica y Norteamérica
        assertEquals(2, continentes.findAllByNombreContainingAndOrdenIsLessThan("américa", (short) 3).size());
        assertTrue(continentes.findAllByNombreContainingAndOrdenIsLessThan("américa", (short) 1).isEmpty());
        assertTrue(continentes.findAllByNombreContainingAndOrdenIsLessThan("antártida", (short) 9).isEmpty());
    }
}
