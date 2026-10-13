package es.safareyes.smartchef.controladores;

import es.safareyes.smartchef.controladores.dto.ComidaDto;
import es.safareyes.smartchef.controladores.dto.NuevaComidaPeticion;
import es.safareyes.smartchef.dtos.ResumenDia;
import es.safareyes.smartchef.excepciones.RecursoNoEncontradoException;
import es.safareyes.smartchef.modelos.PlanComida;
import es.safareyes.smartchef.modelos.Receta;
import es.safareyes.smartchef.modelos.Usuario;
import es.safareyes.smartchef.modelos.catalogos.TipoComida;
import es.safareyes.smartchef.pruebas.Datos;
import es.safareyes.smartchef.repositorios.IPlanComidaRepository;
import es.safareyes.smartchef.repositorios.IRecetaRepository;
import es.safareyes.smartchef.repositorios.ITipoComidaRepository;
import es.safareyes.smartchef.repositorios.IUsuarioRepository;
import es.safareyes.smartchef.servicios.PlanComidaService;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static es.safareyes.smartchef.pruebas.Repos.fake;
import static es.safareyes.smartchef.pruebas.Repos.metodos;
import static es.safareyes.smartchef.pruebas.Repos.opcion;
import static es.safareyes.smartchef.pruebas.Repos.usuario;
import static org.junit.jupiter.api.Assertions.*;

class PlanComidaControllerTest {

    private final LocalDate hoy = LocalDate.now();
    private final Usuario usuario = usuario(1);
    private final Receta espaguetis = Datos.espaguetisAlPomodoro();
    private final List<PlanComida> plan = new ArrayList<>();
    private final Map<Integer, PlanComida> comidas = new HashMap<>();
    private final List<LocalDate> fechasPedidas = new ArrayList<>();
    private final PlanComidaController controlador;

    PlanComidaControllerTest() {
        espaguetis.setId(7);
        espaguetis.setTitulo("Espaguetis al pomodoro");
        usuario.setObjetivoCaloriasDia(2000);
        TipoComida comida = opcion(new TipoComida(), 3, "COMIDA", "Comida");

        var planes = metodos();
        planes.put("findByUsuarioIdAndFechaBetweenOrderByFechaAscTipoComidaOrdenAsc", a -> {
            fechasPedidas.add((LocalDate) a[1]);
            fechasPedidas.add((LocalDate) a[2]);
            return plan;
        });
        planes.put("findById", a -> Optional.ofNullable(comidas.get(a[0])));
        var usuarios = metodos();
        usuarios.put("findById", a -> Optional.of(usuario));
        var recetas = metodos();
        recetas.put("findById", a -> Optional.of(espaguetis));
        var tipos = metodos();
        tipos.put("findByCodigo", a -> Optional.of(comida));
        controlador = new PlanComidaController(new PlanComidaService(fake(IPlanComidaRepository.class, planes),
                fake(IUsuarioRepository.class, usuarios), fake(IRecetaRepository.class, recetas), fake(ITipoComidaRepository.class, tipos)));
    }

    @Test
    void planificarDevuelveLaComidaConSuAviso() {
        ComidaDto dto = controlador.planificar(1, new NuevaComidaPeticion(hoy, "COMIDA", 7, null, 2, LocalTime.of(13, 0)));
        assertEquals("Comida", dto.tipoComida());
        assertEquals("Espaguetis al pomodoro", dto.descripcion());
        assertEquals(7, dto.idReceta());
        assertEquals(LocalTime.of(13, 0), dto.horaAviso());
        assertTrue(dto.notificar());
        assertFalse(dto.completada());
    }

    @Test
    void unaComidaLibreUsaSuDescripcion() {
        ComidaDto dto = controlador.planificar(1, new NuevaComidaPeticion(hoy, "COMIDA", null, "Cena fuera", 1, null));
        assertEquals("Cena fuera", dto.descripcion());
        assertNull(dto.idReceta());
        assertFalse(dto.notificar());
    }

    @Test
    void sinFechaLaSemanaEmpiezaHoyYDura7Dias() {
        controlador.semana(1, null);
        assertEquals(List.of(hoy, hoy.plusDays(6)), fechasPedidas);
        fechasPedidas.clear();
        controlador.semana(1, LocalDate.of(2026, 10, 8));
        assertEquals(List.of(LocalDate.of(2026, 10, 8), LocalDate.of(2026, 10, 14)), fechasPedidas);
    }

    @Test
    void elResumenDelDiaComparaConElObjetivo() {
        PlanComida p = new PlanComida();
        p.setReceta(espaguetis);
        p.setRaciones((short) 2);
        plan.add(p);
        ResumenDia resumen = controlador.resumen(1, null);
        assertEquals(hoy, resumen.getFecha());
        assertEquals("961.0", resumen.getCaloriasPlanificadas().toPlainString());
        assertEquals("1039.0", resumen.getDiferencia().toPlainString());
    }

    @Test
    void completarSoloValeParaComidasPropias() {
        PlanComida p = new PlanComida();
        p.setUsuario(usuario);
        p.setReceta(espaguetis);
        p.setRaciones((short) 1);
        comidas.put(1, p);
        assertTrue(controlador.completar(1, 1).completada());
        assertThrows(RecursoNoEncontradoException.class, () -> controlador.completar(2, 1));
        assertThrows(RecursoNoEncontradoException.class, () -> controlador.completar(1, 99));
    }
}
