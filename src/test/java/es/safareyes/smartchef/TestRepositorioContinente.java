package es.safareyes.smartchef;

import es.safareyes.smartchef.dtos.IngredientesPorCategoria;
import es.safareyes.smartchef.dtos.RecetasPorTipoPlato;
import es.safareyes.smartchef.modelos.catalogos.Continente;
import es.safareyes.smartchef.repositorios.ICategoriaIngredienteRepository;
import es.safareyes.smartchef.repositorios.IContinenteRepository;
import es.safareyes.smartchef.repositorios.IRecetaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class TestRepositorioContinente {

    @Autowired
    private IContinenteRepository repository;

    @Autowired
    private ICategoriaIngredienteRepository categoriaRepository;

    @Autowired
    private IRecetaRepository recetaRepository;

    @Test
    void consultarContinentes() {
        // Con los datos de ejemplo: Sudamérica (1), Centroamérica (2) y Norteamérica (3)
        List<Continente> americas = repository.findAllByNombreContainingAndOrdenIsLessThan("américa", (short) 4);
        assertEquals(3, americas.size());
    }

    @Test
    void consultarIngredientesPorCategoria() {
        List<IngredientesPorCategoria> informe = categoriaRepository.contarIngredientesPorCategoria();
        assertFalse(informe.isEmpty());
        assertTrue(informe.stream().anyMatch(i -> i.getCategoria().equals("Verduras y hortalizas")
                && i.getIngredientes() == 2));
    }

    @Test
    void consultarRecetasPorTipoPlato() {
        List<RecetasPorTipoPlato> informe = recetaRepository.contarRecetasPorTipoPlato();
        assertTrue(informe.stream().anyMatch(i -> i.getTipoPlato().equals("Plato principal")
                && i.getRecetas() == 2));
    }
}
