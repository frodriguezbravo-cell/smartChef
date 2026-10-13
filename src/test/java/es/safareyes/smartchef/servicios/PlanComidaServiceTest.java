package es.safareyes.smartchef.servicios;

import es.safareyes.smartchef.dtos.ResumenDia;
import es.safareyes.smartchef.excepciones.RecursoNoEncontradoException;
import es.safareyes.smartchef.excepciones.ReglaNegocioException;
import es.safareyes.smartchef.modelos.PlanComida;
import es.safareyes.smartchef.modelos.Receta;
import es.safareyes.smartchef.modelos.Usuario;
import es.safareyes.smartchef.modelos.catalogos.TipoComida;
import es.safareyes.smartchef.pruebas.Datos;
import es.safareyes.smartchef.repositorios.IPlanComidaRepository;
import es.safareyes.smartchef.repositorios.IRecetaRepository;
import es.safareyes.smartchef.repositorios.ITipoComidaRepository;
import es.safareyes.smartchef.repositorios.IUsuarioRepository;
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

class PlanComidaServiceTest {

    private final LocalDate hoy = LocalDate.now();
    private final Usuario usuario = usuario(1);
    private final Receta espaguetis = Datos.espaguetisAlPomodoro();
    private final TipoComida comida = opcion(new TipoComida(), 3, "COMIDA", "Comida");
    private final TipoComida cena = opcion(new TipoComida(), 5, "CENA", "Cena");
    private final List<PlanComida> planGuardado = new ArrayList<>();
    private final Map<Integer, PlanComida> comidas = new HashMap<>();
    private final PlanComidaService servicio;

    PlanComidaServiceTest() {
        espaguetis.setId(7);
        usuario.setObjetivoCaloriasDia(2000);

        var planes = metodos();
        planes.put("findByUsuarioIdAndFechaBetweenOrderByFechaAscTipoComidaOrdenAsc", a -> planGuardado);
        planes.put("findById", a -> Optional.ofNullable(comidas.get(a[0])));
        var usuarios = metodos();
        usuarios.put("findById", a -> Optional.of(usuario));
        var recetas = metodos();
        recetas.put("findById", a -> a[0].equals(7) ? Optional.of(espaguetis) : Optional.empty());
        var tipos = metodos();
        tipos.put("findByCodigo", a -> a[0].equals("COMIDA") ? Optional.of(comida) : Optional.empty());

        servicio = new PlanComidaService(fake(IPlanComidaRepository.class, planes), fake(IUsuarioRepository.class, usuarios),
                fake(IRecetaRepository.class, recetas), fake(ITipoComidaRepository.class, tipos));
    }

    @Test
    void hayQueIndicarFechaRacionesYRecetaODescripcion() {
        assertThrows(ReglaNegocioException.class, () -> servicio.planificar(1, hoy, "COMIDA", null, "  ", 1, null));
        assertThrows(ReglaNegocioException.class, () -> servicio.planificar(1, hoy, "COMIDA", 7, null, 0, null));
        assertThrows(ReglaNegocioException.class, () -> servicio.planificar(1, null, "COMIDA", 7, null, 1, null));
    }

    @Test
    void rechazaTiposDeComidaYRecetasQueNoExisten() {
        assertThrows(RecursoNoEncontradoException.class, () -> servicio.planificar(1, hoy, "BRUNCH", 7, null, 1, null));
        assertThrows(RecursoNoEncontradoException.class, () -> servicio.planificar(1, hoy, "COMIDA", 99, null, 1, null));
    }

    @Test
    void conHoraDeAvisoSeActivaLaNotificacion() {
        PlanComida plan = servicio.planificar(1, hoy, "COMIDA", 7, null, 2, LocalTime.of(13, 0));
        assertTrue(plan.getNotificar());
        assertEquals(2, plan.getRaciones().intValue());
        assertNotNull(plan.getReceta());
    }

    @Test
    void sinHoraNoHayAvisoYLaDescripcionSeRecorta() {
        PlanComida plan = servicio.planificar(1, hoy, "COMIDA", null, " Cena fuera ", 1, null);
        assertFalse(plan.getNotificar());
        assertEquals("Cena fuera", plan.getDescripcionLibre());
    }

    @Test
    void elResumenDelDiaSumaLasCaloriasYLasCompara() {
        planGuardado.add(plan(espaguetis, null, 2, comida));
        planGuardado.add(plan(null, "Cena fuera", 1, cena));

        ResumenDia resumen = servicio.resumenDia(1, hoy);
        assertEquals(2, resumen.getTotalComidas());
        assertEquals("961.0", resumen.getCaloriasPlanificadas().toPlainString());     // 2 raciones de espaguetis
        assertEquals(1, resumen.getComidasSinDatos());                                // la cena fuera no tiene datos
        assertEquals("1039.0", resumen.getDiferencia().toPlainString());              // 2000 - 961,0
    }

    @Test
    void sinObjetivoNoHayDiferencia() {
        usuario.setObjetivoCaloriasDia(null);
        assertNull(servicio.resumenDia(1, hoy).getDiferencia());
    }

    private PlanComida plan(Receta receta, String descripcion, int raciones, TipoComida tipo) {
        PlanComida p = new PlanComida();
        p.setReceta(receta);
        p.setDescripcionLibre(descripcion);
        p.setRaciones((short) raciones);
        p.setTipoComida(tipo);
        return p;
    }

    @Test
    void unaComidaAjenaSeTrataComoSiNoExistiera() {
        PlanComida plan = plan(espaguetis, null, 2, comida);
        plan.setUsuario(usuario);
        comidas.put(1, plan);
        assertSame(plan, servicio.comidaDelUsuario(1, 1));
        assertThrows(RecursoNoEncontradoException.class, () -> servicio.comidaDelUsuario(2, 1));
        assertThrows(RecursoNoEncontradoException.class, () -> servicio.comidaDelUsuario(1, 99));
    }
}
