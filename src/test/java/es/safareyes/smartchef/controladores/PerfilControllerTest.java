package es.safareyes.smartchef.controladores;

import es.safareyes.smartchef.controladores.dto.AlergiaDto;
import es.safareyes.smartchef.controladores.dto.AlergiaPeticion;
import es.safareyes.smartchef.controladores.dto.DietasPeticion;
import es.safareyes.smartchef.controladores.dto.OpcionCatalogoDto;
import es.safareyes.smartchef.excepciones.RecursoNoEncontradoException;
import es.safareyes.smartchef.modelos.Usuario;
import es.safareyes.smartchef.modelos.UsuarioAlergeno;
import es.safareyes.smartchef.modelos.catalogos.Alergeno;
import es.safareyes.smartchef.modelos.catalogos.Gravedad;
import es.safareyes.smartchef.pruebas.Datos;
import es.safareyes.smartchef.repositorios.IAlergenoRepository;
import es.safareyes.smartchef.repositorios.IDietaRepository;
import es.safareyes.smartchef.repositorios.IGravedadRepository;
import es.safareyes.smartchef.repositorios.IUsuarioAlergenoRepository;
import es.safareyes.smartchef.repositorios.IUsuarioRepository;
import es.safareyes.smartchef.servicios.PerfilUsuarioService;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static es.safareyes.smartchef.pruebas.Repos.fake;
import static es.safareyes.smartchef.pruebas.Repos.metodos;
import static es.safareyes.smartchef.pruebas.Repos.opcion;
import static es.safareyes.smartchef.pruebas.Repos.usuario;
import static org.junit.jupiter.api.Assertions.*;

class PerfilControllerTest {

    private final Usuario usuario = usuario(1);
    private final List<UsuarioAlergeno> baseDeDatos = new ArrayList<>();
    private final PerfilController controlador;

    PerfilControllerTest() {
        Alergeno gluten = opcion(new Alergeno(), 1, "GLUTEN", "Cereales con gluten");
        Gravedad leve = opcion(new Gravedad(), 2, "ALERGIA_LEVE", "Alergia leve");
        Gravedad grave = opcion(new Gravedad(), 3, "ALERGIA_GRAVE", "Alergia grave");
        var usuarios = metodos();
        usuarios.put("findById", a -> Optional.of(usuario));
        var registros = metodos();
        registros.put("findByUsuarioId", a -> baseDeDatos);
        registros.put("findByUsuarioIdAndAlergenoId", a -> baseDeDatos.stream().findFirst());
        registros.put("save", a -> {
            if (!baseDeDatos.contains(a[0])) {
                baseDeDatos.add((UsuarioAlergeno) a[0]);
            }
            return a[0];
        });
        registros.put("delete", a -> {
            baseDeDatos.remove(a[0]);
            return null;
        });
        var alergenos = metodos();
        alergenos.put("findByCodigo", a -> a[0].equals("GLUTEN") ? Optional.of(gluten) : Optional.empty());
        var gravedades = metodos();
        gravedades.put("findByCodigo", a -> a[0].equals("ALERGIA_LEVE") ? Optional.of(leve)
                : a[0].equals("ALERGIA_GRAVE") ? Optional.of(grave) : Optional.empty());
        var dietas = metodos();
        dietas.put("findByCodigo", a -> Optional.of(Datos.dieta((String) a[0])));
        controlador = new PerfilController(new PerfilUsuarioService(fake(IUsuarioRepository.class, usuarios),
                fake(IUsuarioAlergenoRepository.class, registros), fake(IAlergenoRepository.class, alergenos),
                fake(IGravedadRepository.class, gravedades), fake(IDietaRepository.class, dietas)));
    }

    @Test
    void sinCuerpoLaAlergiaEsLeve() {
        AlergiaDto dto = controlador.anadirAlergia(1, "GLUTEN", null);
        assertEquals(new AlergiaDto("GLUTEN", "Cereales con gluten", "Alergia leve", null), dto);
        assertEquals(1, controlador.alergias(1).size());
    }

    @Test
    void conCuerpoSeGuardanGravedadYNotas() {
        AlergiaDto dto = controlador.anadirAlergia(1, "GLUTEN", new AlergiaPeticion("ALERGIA_GRAVE", "celíaco"));
        assertEquals("Alergia grave", dto.gravedad());
        assertEquals("celíaco", dto.notas());
    }

    @Test
    void quitarLaAlergiaLaBorra() {
        controlador.anadirAlergia(1, "GLUTEN", null);
        controlador.quitarAlergia(1, "GLUTEN");
        assertTrue(controlador.alergias(1).isEmpty());
    }

    @Test
    void laRespuestaDeUnAlergenoDesconocidoEsNoEncontrado() {
        assertThrows(RecursoNoEncontradoException.class, () -> controlador.anadirAlergia(1, "NOEXISTE", null));
    }

    @Test
    void lasDietasSeEstablecenYSeLeen() {
        List<OpcionCatalogoDto> establecidas = controlador.establecerDietas(1, new DietasPeticion(List.of("VEGANA", "SIN_GLUTEN")));
        assertEquals(2, establecidas.size());
        assertEquals("VEGANA", controlador.dietas(1).get(0).codigo());
    }
}
