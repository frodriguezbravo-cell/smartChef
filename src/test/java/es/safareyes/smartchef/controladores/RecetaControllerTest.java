package es.safareyes.smartchef.controladores;

import es.safareyes.smartchef.controladores.dto.AlergiaDto;
import es.safareyes.smartchef.controladores.dto.RecetaDetalleDto;
import es.safareyes.smartchef.controladores.dto.RecetaResumenDto;
import es.safareyes.smartchef.dtos.IngredienteEscalado;
import es.safareyes.smartchef.excepciones.RecursoNoEncontradoException;
import es.safareyes.smartchef.excepciones.ReglaNegocioException;
import es.safareyes.smartchef.modelos.Receta;
import es.safareyes.smartchef.modelos.Usuario;
import es.safareyes.smartchef.modelos.UsuarioAlergeno;
import es.safareyes.smartchef.modelos.catalogos.Alergeno;
import es.safareyes.smartchef.modelos.catalogos.Gravedad;
import es.safareyes.smartchef.modelos.catalogos.TipoPlato;
import es.safareyes.smartchef.pruebas.Datos;
import es.safareyes.smartchef.repositorios.IRecetaRepository;
import es.safareyes.smartchef.repositorios.IUsuarioAlergenoRepository;
import es.safareyes.smartchef.repositorios.IUsuarioRepository;
import es.safareyes.smartchef.servicios.RecetaService;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static es.safareyes.smartchef.pruebas.Repos.fake;
import static es.safareyes.smartchef.pruebas.Repos.metodos;
import static es.safareyes.smartchef.pruebas.Repos.opcion;
import static es.safareyes.smartchef.pruebas.Repos.usuario;
import static org.junit.jupiter.api.Assertions.*;

class RecetaControllerTest {

    private final Receta espaguetis = Datos.espaguetisAlPomodoro();       // 2 raciones
    private final Usuario usuario = usuario(1);
    private final Alergeno gluten = opcion(new Alergeno(), 1, "GLUTEN", "Cereales con gluten");
    private final RecetaController controlador;

    RecetaControllerTest() {
        espaguetis.setId(7);
        espaguetis.setSlug("espaguetis-al-pomodoro");
        espaguetis.setTiempoPreparacionMin((short) 10);
        espaguetis.setTiempoCoccionMin((short) 20);
        espaguetis.setTipoPlato(opcion(new TipoPlato(), 5, "PRINCIPAL", "Plato principal"));
        espaguetis.getIngredientes().get(0).getIngrediente().getAlergenos().add(gluten);

        Gravedad grave = opcion(new Gravedad(), 3, "ALERGIA_GRAVE", "Alergia grave");
        grave.setOrden((short) 3);
        UsuarioAlergeno alergia = new UsuarioAlergeno();
        alergia.setAlergeno(gluten);
        alergia.setGravedad(grave);
        alergia.setNotas("celíaco");

        var recetas = metodos();
        recetas.put("findById", a -> a[0].equals(7) ? Optional.of(espaguetis) : Optional.empty());
        recetas.put("findByTituloContainingIgnoreCaseAndPublicadaTrue", a -> List.of(espaguetis));
        recetas.put("buscarRapidas", a -> List.of(espaguetis));
        recetas.put("findByPublicadaTrue", a -> List.of(espaguetis));
        var usuarios = metodos();
        usuarios.put("findById", a -> Optional.of(usuario));
        var alergenos = metodos();
        alergenos.put("findByUsuarioId", a -> List.of(alergia));
        controlador = new RecetaController(new RecetaService(fake(IRecetaRepository.class, recetas),
                fake(IUsuarioRepository.class, usuarios), fake(IUsuarioAlergenoRepository.class, alergenos)));
    }

    @Test
    void elResumenIncluyeElTiempoTotalYLasDietas() {
        List<RecetaResumenDto> resultado = controlador.buscar("espag");
        assertEquals(1, resultado.size());
        RecetaResumenDto r = resultado.get(0);
        assertEquals("Espaguetis al pomodoro", r.titulo());
        assertEquals(30, r.tiempoTotalMin());                        // 10 de preparación + 20 de cocción
        assertEquals("Plato principal", r.tipoPlato());
        assertEquals(List.of("VEGETARIANA"), r.dietas());
        assertEquals(1, controlador.rapidas(30).size());
    }

    @Test
    void sinRacionesElDetalleUsaLasDeLaReceta() {
        RecetaDetalleDto d = controlador.detalle(7, null);
        assertEquals(2, d.racionesSolicitadas());
        assertEquals(5, d.ingredientes().size());
        assertEquals("961.0", d.nutricion().getCaloriasKcal().toPlainString());
        assertEquals(List.of("Cereales con gluten"), d.alergenos());
    }

    @Test
    void conOtrasRacionesSeEscalanIngredientesYNutricion() {
        RecetaDetalleDto d = controlador.detalle(7, 4);
        assertEquals(4, d.racionesSolicitadas());
        IngredienteEscalado tomate = d.ingredientes().stream().filter(i -> i.getNombre().equals("Tomate")).findFirst().orElseThrow();
        assertEquals("800.00", tomate.getCantidad().toPlainString());
        assertEquals("1922.1", d.nutricion().getCaloriasKcal().toPlainString());
    }

    @Test
    void rechazaRacionesNoValidasYRecetasInexistentes() {
        assertThrows(ReglaNegocioException.class, () -> controlador.detalle(7, 0));
        assertThrows(RecursoNoEncontradoException.class, () -> controlador.detalle(99, null));
    }

    @Test
    void losAvisosDicenQueAlergiasDelUsuarioAfectanALaReceta() {
        List<AlergiaDto> avisos = controlador.avisos(1, 7);
        assertEquals(1, avisos.size());
        assertEquals(new AlergiaDto("GLUTEN", "Cereales con gluten", "Alergia grave", "celíaco"), avisos.get(0));
    }

    @Test
    void lasRecetasSegurasSeConvierteEnResumenes() {
        assertEquals(1, controlador.seguras(1).size());
    }
}
