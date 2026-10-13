package es.safareyes.smartchef.repositorios;

import es.safareyes.smartchef.modelos.CategoriaUtensilio;
import es.safareyes.smartchef.modelos.Utensilio;
import es.safareyes.smartchef.pruebas.PruebaBaseDatos;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Categorías de utensilios (con subcategorías), utensilios, marcas y orígenes. */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UtensilioRepositoriesTest extends PruebaBaseDatos {

    @Autowired private ICategoriaUtensilioRepository categorias;
    @Autowired private IUtensilioRepository utensilios;
    @Autowired private IMarcaRepository marcas;
    @Autowired private IOrigenRepository origenes;

    @Test
    void lasCategoriasRaizNoTienenPadreYVanOrdenadas() {
        List<CategoriaUtensilio> raiz = categorias.findByCategoriaPadreIsNullOrderByOrdenAsc();
        assertEquals(4, raiz.size());
        assertEquals("Electrodomésticos", raiz.get(0).getNombre());
        assertEquals("Menaje de cocción", raiz.get(1).getNombre());
        assertEquals("Medición", raiz.get(3).getNombre());
        assertTrue(raiz.stream().allMatch(c -> c.getCategoriaPadre() == null));
    }

    @Test
    void lasSubcategoriasSeBuscanPorSuPadre() {
        int electrodomesticos = bd().idPor("categoria_utensilio", "nombre", "Electrodomésticos");
        List<CategoriaUtensilio> hijas = categorias.findByCategoriaPadreIdOrderByOrdenAsc(electrodomesticos);
        assertEquals(2, hijas.size());
        assertEquals("Horno", hijas.get(0).getNombre());
        assertEquals("Batidora", hijas.get(1).getNombre());
        assertEquals("Electrodomésticos", hijas.get(0).getCategoriaPadre().getNombre());
        assertTrue(categorias.findByCategoriaPadreIdOrderByOrdenAsc(bd().idPor("categoria_utensilio", "nombre", "Sartenes")).isEmpty());
    }

    @Test
    void losUtensiliosSeBuscanPorCategoriaYPorModelo() {
        int sartenes = bd().idPor("categoria_utensilio", "nombre", "Sartenes");
        List<Utensilio> deSartenes = utensilios.findByCategoriaId(sartenes);
        assertEquals(1, deSartenes.size());
        assertEquals("Sartén antiadherente 28 cm", deSartenes.get(0).getModelo());
        assertEquals("Tefal", deSartenes.get(0).getMarca().getNombre());
        assertTrue(utensilios.findByCategoriaId(bd().idPor("categoria_utensilio", "nombre", "Cuchillos")).isEmpty());

        assertEquals(1, utensilios.findByModeloContainingIgnoreCase("OLLA").size());
        assertEquals(0, new java.math.BigDecimal("6.5").compareTo(utensilios.findByModeloContainingIgnoreCase("olla").get(0).getCapacidadL()));
        assertTrue(utensilios.findByModeloContainingIgnoreCase("zzz").isEmpty());
    }

    @Test
    void buscaMarcasPorNombre() {
        assertEquals("Francia", marcas.findByNombre("Tefal").orElseThrow().getPais());
        assertTrue(marcas.findByNombre("Bosch").isPresent());
        assertTrue(marcas.findByNombre("Inexistente").isEmpty());
    }

    @Test
    void losOrigenesSeFiltranPorContinente() {
        List<es.safareyes.smartchef.modelos.Origen> europa = origenes.findByContinenteCodigo("EUROPA");
        assertEquals(2, europa.size());
        assertTrue(europa.stream().anyMatch(o -> o.getNombre().equals("Italia")));
        assertEquals("Japón", origenes.findByContinenteCodigo("ASIA").get(0).getNombre());
        assertEquals(1, origenes.findByContinenteCodigo("AMERICA_CENTRAL").size());
        assertTrue(origenes.findByContinenteCodigo("AFRICA").isEmpty());
    }
}
