package es.safareyes.smartchef.servicios;

import es.safareyes.smartchef.excepciones.RecursoNoEncontradoException;
import es.safareyes.smartchef.modelos.catalogos.Catalogo;
import es.safareyes.smartchef.modelos.catalogos.Dieta;
import es.safareyes.smartchef.repositorios.IAlergenoRepository;
import es.safareyes.smartchef.repositorios.IDietaRepository;
import es.safareyes.smartchef.repositorios.IGravedadRepository;
import es.safareyes.smartchef.repositorios.IMotivoBajaRepository;
import es.safareyes.smartchef.repositorios.ITipoComidaRepository;
import es.safareyes.smartchef.repositorios.IUbicacionDespensaRepository;
import es.safareyes.smartchef.repositorios.IUnidadMedidaRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static es.safareyes.smartchef.pruebas.Repos.fake;
import static es.safareyes.smartchef.pruebas.Repos.metodos;
import static es.safareyes.smartchef.pruebas.Repos.opcion;
import static org.junit.jupiter.api.Assertions.*;

class CatalogoServiceTest {

    private final CatalogoService servicio;

    CatalogoServiceTest() {
        Dieta vegana = opcion(new Dieta(), 3, "VEGANA", "Vegana");
        vegana.setOrden((short) 3);
        vegana.setActivo(true);
        Dieta omnivora = opcion(new Dieta(), 1, "OMNIVORA", "Omnívora");
        omnivora.setOrden((short) 1);
        omnivora.setActivo(true);
        Dieta retirada = opcion(new Dieta(), 9, "RETIRADA", "Retirada");
        retirada.setOrden((short) 2);
        retirada.setActivo(false);
        var dietas = metodos();
        dietas.put("findAll", a -> List.of(vegana, retirada, omnivora));

        servicio = new CatalogoService(fake(IDietaRepository.class, dietas), fake(IAlergenoRepository.class),
                fake(IGravedadRepository.class), fake(IUnidadMedidaRepository.class), fake(ITipoComidaRepository.class),
                fake(IUbicacionDespensaRepository.class), fake(IMotivoBajaRepository.class));
    }

    @Test
    void devuelveLasOpcionesActivasEnElOrdenDeLaBaseDeDatos() {
        List<Catalogo> dietas = servicio.opciones("dietas");
        assertEquals(2, dietas.size());                         // la opción desactivada no sale
        assertEquals("OMNIVORA", dietas.get(0).getCodigo());
        assertEquals("VEGANA", dietas.get(1).getCodigo());
    }

    @Test
    void unCatalogoDesconocidoNoSeEncuentra() {
        assertThrows(RecursoNoEncontradoException.class, () -> servicio.opciones("colores"));
    }

    @Test
    void todosLosNombresPublicadosSonValidos() {
        for (String nombre : CatalogoService.NOMBRES) {
            assertNotNull(servicio.opciones(nombre));
        }
    }
}
