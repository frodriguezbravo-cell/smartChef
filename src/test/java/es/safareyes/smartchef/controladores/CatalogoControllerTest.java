package es.safareyes.smartchef.controladores;

import es.safareyes.smartchef.controladores.dto.OpcionCatalogoDto;
import es.safareyes.smartchef.excepciones.RecursoNoEncontradoException;
import es.safareyes.smartchef.modelos.catalogos.Dieta;
import es.safareyes.smartchef.repositorios.IAlergenoRepository;
import es.safareyes.smartchef.repositorios.IDietaRepository;
import es.safareyes.smartchef.repositorios.IGravedadRepository;
import es.safareyes.smartchef.repositorios.IMotivoBajaRepository;
import es.safareyes.smartchef.repositorios.ITipoComidaRepository;
import es.safareyes.smartchef.repositorios.IUbicacionDespensaRepository;
import es.safareyes.smartchef.repositorios.IUnidadMedidaRepository;
import es.safareyes.smartchef.servicios.CatalogoService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static es.safareyes.smartchef.pruebas.Repos.fake;
import static es.safareyes.smartchef.pruebas.Repos.metodos;
import static es.safareyes.smartchef.pruebas.Repos.opcion;
import static es.safareyes.smartchef.pruebas.Repos.usuario;
import static org.junit.jupiter.api.Assertions.*;

class CatalogoControllerTest {

    private final CatalogoController controlador;

    CatalogoControllerTest() {
        Dieta vegana = opcion(new Dieta(), 3, "VEGANA", "Vegana");
        vegana.setOrden((short) 2);
        vegana.setActivo(true);
        Dieta omnivora = opcion(new Dieta(), 1, "OMNIVORA", "Omnívora");
        omnivora.setOrden((short) 1);
        omnivora.setActivo(true);
        omnivora.setDescripcion("Come de todo");
        var dietas = metodos();
        dietas.put("findAll", a -> List.of(vegana, omnivora));
        controlador = new CatalogoController(new CatalogoService(fake(IDietaRepository.class, dietas), fake(IAlergenoRepository.class),
                fake(IGravedadRepository.class), fake(IUnidadMedidaRepository.class), fake(ITipoComidaRepository.class),
                fake(IUbicacionDespensaRepository.class), fake(IMotivoBajaRepository.class)));
    }

    @Test
    void devuelveCodigoNombreYDescripcionEnOrden() {
        List<OpcionCatalogoDto> dietas = controlador.opciones("dietas");
        assertEquals(2, dietas.size());
        assertEquals(new OpcionCatalogoDto("OMNIVORA", "Omnívora", "Come de todo"), dietas.get(0));
        assertEquals("VEGANA", dietas.get(1).codigo());
    }

    @Test
    void unCatalogoQueNoExisteLanzaNoEncontrado() {
        assertThrows(RecursoNoEncontradoException.class, () -> controlador.opciones("colores"));
    }
}
