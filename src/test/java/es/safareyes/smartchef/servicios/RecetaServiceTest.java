package es.safareyes.smartchef.servicios;

import es.safareyes.smartchef.excepciones.RecursoNoEncontradoException;
import es.safareyes.smartchef.excepciones.ReglaNegocioException;
import es.safareyes.smartchef.modelos.Receta;
import es.safareyes.smartchef.modelos.Usuario;
import es.safareyes.smartchef.pruebas.Datos;
import es.safareyes.smartchef.repositorios.IRecetaRepository;
import es.safareyes.smartchef.repositorios.IUsuarioAlergenoRepository;
import es.safareyes.smartchef.repositorios.IUsuarioRepository;
import es.safareyes.smartchef.modelos.Ingrediente;
import es.safareyes.smartchef.modelos.UsuarioAlergeno;
import es.safareyes.smartchef.modelos.catalogos.Alergeno;
import es.safareyes.smartchef.modelos.catalogos.Gravedad;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static es.safareyes.smartchef.pruebas.Repos.fake;
import static es.safareyes.smartchef.pruebas.Repos.metodos;
import static es.safareyes.smartchef.pruebas.Repos.opcion;
import static es.safareyes.smartchef.pruebas.Repos.usuario;
import static org.junit.jupiter.api.Assertions.*;

class RecetaServiceTest {

    private final Usuario usuario = usuario(1);
    private final Receta vegana = Datos.receta("Ensalada", 2, "VEGANA");
    private final Receta conCarne = Datos.receta("Pollo", 2);
    private final List<String> llamadas = new ArrayList<>();
    private final List<String> alergias = new ArrayList<>();
    private final RecetaService servicio;

    RecetaServiceTest() {
        vegana.setId(10);
        conCarne.setId(11);
        usuario.getDietas().add(Datos.dieta("VEGETARIANA"));

        var recetas = metodos();
        recetas.put("findByPublicadaTrue", a -> {
            llamadas.add("todas");
            return List.of(vegana, conCarne);
        });
        recetas.put("buscarSinAlergenos", a -> {
            llamadas.add("sinAlergenos" + a[0]);
            return List.of(vegana);
        });
        var usuarios = metodos();
        usuarios.put("findById", a -> a[0].equals(1) ? Optional.of(usuario) : Optional.empty());
        var alergenos = metodos();
        alergenos.put("codigosDeAlergenos", a -> alergias);

        servicio = new RecetaService(fake(IRecetaRepository.class, recetas),
                fake(IUsuarioRepository.class, usuarios), fake(IUsuarioAlergenoRepository.class, alergenos));
    }

    @Test
    void sinAlergiasSeParteDeTodasLasPublicadasYSeFiltraPorDieta() {
        List<Receta> seguras = servicio.recetasSegurasPara(1);
        assertEquals(1, seguras.size());
        assertEquals(10, seguras.get(0).getId());              // la vegana vale para quien es vegetariano
        assertEquals(List.of("todas"), llamadas);
    }

    @Test
    void conAlergiasSeUsanSusCodigosYNoSeCargaTodo() {
        alergias.add("GLUTEN");
        assertEquals(1, servicio.recetasSegurasPara(1).size());
        assertEquals(List.of("sinAlergenos[GLUTEN]"), llamadas);
    }

    @Test
    void unUsuarioInexistenteDaUnMensajeClaro() {
        RecursoNoEncontradoException e = assertThrows(RecursoNoEncontradoException.class, () -> servicio.recetasSegurasPara(99));
        assertEquals("Usuario no encontrado/a: 99", e.getMessage());
    }

    @Test
    void rechazaDatosNoValidos() {
        assertThrows(ReglaNegocioException.class, () -> servicio.recetasRapidas(0));
        assertThrows(ReglaNegocioException.class, () -> servicio.nutricion(1, 0));
        assertThrows(ReglaNegocioException.class, () -> servicio.ingredientesPara(1, -2));
    }

    @Test
    void unaRecetaInexistenteNoSeEncuentra() {
        assertThrows(RecursoNoEncontradoException.class, () -> servicio.ingredientesPara(5, 2));
    }

    @Test
    void losAvisosDeUnaRecetaSoloIncluyenLasAlergiasDelUsuarioQueLaAfectan() {
        Alergeno gluten = opcion(new Alergeno(), 1, "GLUTEN", "Cereales con gluten");
        Alergeno lacteos = opcion(new Alergeno(), 7, "LACTEOS", "Leche y lácteos");
        Alergeno huevos = opcion(new Alergeno(), 3, "HUEVOS", "Huevos");
        Receta espaguetis = Datos.espaguetisAlPomodoro();               // espaguetis + parmesano (opcional)
        espaguetis.setId(7);
        espaguetis.getIngredientes().get(0).getIngrediente().getAlergenos().add(gluten);
        espaguetis.getIngredientes().get(4).getIngrediente().getAlergenos().add(lacteos);

        List<UsuarioAlergeno> registradas = List.of(
                alergia(lacteos, 1, "INTOLERANCIA"), alergia(huevos, 1, "ALERGIA_GRAVE"), alergia(gluten, 3, "ALERGIA_GRAVE"));
        var recetas = metodos();
        recetas.put("findById", a -> Optional.of(espaguetis));
        var usuarios = metodos();
        usuarios.put("findById", a -> Optional.of(usuario));
        var alergenos = metodos();
        alergenos.put("findByUsuarioId", a -> registradas);
        RecetaService s = new RecetaService(fake(IRecetaRepository.class, recetas), fake(IUsuarioRepository.class, usuarios),
                fake(IUsuarioAlergenoRepository.class, alergenos));

        List<UsuarioAlergeno> avisos = s.avisosDeAlergenos(1, 7);
        assertEquals(2, avisos.size());                                          // los huevos no están en la receta
        assertEquals("GLUTEN", avisos.get(0).getAlergeno().getCodigo());         // la más grave, primero
        assertEquals("LACTEOS", avisos.get(1).getAlergeno().getCodigo());        // aunque el parmesano sea opcional
    }

    private UsuarioAlergeno alergia(Alergeno alergeno, int ordenGravedad, String codigoGravedad) {
        UsuarioAlergeno a = new UsuarioAlergeno();
        a.setAlergeno(alergeno);
        Gravedad gravedad = opcion(new Gravedad(), ordenGravedad, codigoGravedad, codigoGravedad);
        gravedad.setOrden((short) ordenGravedad);
        a.setGravedad(gravedad);
        return a;
    }
}
