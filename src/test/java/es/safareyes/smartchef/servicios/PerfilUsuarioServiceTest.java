package es.safareyes.smartchef.servicios;

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
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static es.safareyes.smartchef.pruebas.Repos.fake;
import static es.safareyes.smartchef.pruebas.Repos.metodos;
import static es.safareyes.smartchef.pruebas.Repos.opcion;
import static es.safareyes.smartchef.pruebas.Repos.usuario;
import static org.junit.jupiter.api.Assertions.*;

class PerfilUsuarioServiceTest {

    private final Usuario usuario = usuario(1);
    private final List<UsuarioAlergeno> baseDeDatos = new ArrayList<>();
    private final PerfilUsuarioService servicio;

    PerfilUsuarioServiceTest() {
        Alergeno gluten = opcion(new Alergeno(), 1, "GLUTEN", "Cereales con gluten");
        Gravedad leve = opcion(new Gravedad(), 2, "ALERGIA_LEVE", "Alergia leve");
        Gravedad grave = opcion(new Gravedad(), 3, "ALERGIA_GRAVE", "Alergia grave");

        var usuarios = metodos();
        usuarios.put("findById", a -> Optional.of(usuario));
        var registros = metodos();
        registros.put("findByUsuarioIdAndAlergenoId",
                a -> baseDeDatos.stream().filter(r -> r.getAlergeno().getId().equals(a[1])).findFirst());
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
        dietas.put("findByCodigo", a -> a[0].equals("INVENTADA") ? Optional.empty() : Optional.of(Datos.dieta((String) a[0])));

        servicio = new PerfilUsuarioService(fake(IUsuarioRepository.class, usuarios),
                fake(IUsuarioAlergenoRepository.class, registros), fake(IAlergenoRepository.class, alergenos),
                fake(IGravedadRepository.class, gravedades), fake(IDietaRepository.class, dietas));
    }

    @Test
    void unaAlergiaNuevaSinGravedadEsLeve() {
        servicio.anadirAlergeno(1, "GLUTEN", null, null);
        assertEquals(1, baseDeDatos.size());
        assertEquals("ALERGIA_LEVE", baseDeDatos.get(0).getGravedad().getCodigo());
    }

    @Test
    void repetirUnaAlergiaLaActualizaSinDuplicarla() {
        servicio.anadirAlergeno(1, "GLUTEN", null, null);
        servicio.anadirAlergeno(1, "GLUTEN", "ALERGIA_GRAVE", "celíaco");
        assertEquals(1, baseDeDatos.size());
        assertEquals("ALERGIA_GRAVE", baseDeDatos.get(0).getGravedad().getCodigo());
        assertEquals("celíaco", baseDeDatos.get(0).getNotas());
    }

    @Test
    void quitarUnaAlergiaLaBorra() {
        servicio.anadirAlergeno(1, "GLUTEN", null, null);
        servicio.quitarAlergeno(1, "GLUTEN");
        assertTrue(baseDeDatos.isEmpty());
    }

    @Test
    void rechazaAlergenosYGravedadesQueNoExisten() {
        assertThrows(RecursoNoEncontradoException.class, () -> servicio.anadirAlergeno(1, "NOEXISTE", null, null));
        assertThrows(RecursoNoEncontradoException.class, () -> servicio.anadirAlergeno(1, "GLUTEN", "MUY_GRAVE", null));
    }

    @Test
    void lasDietasSeSustituyenYSeQuitanLasRepetidas() {
        servicio.establecerDietas(1, List.of("VEGANA", "VEGANA", "SIN_GLUTEN"));
        assertEquals(2, usuario.getDietas().size());
        servicio.establecerDietas(1, List.of());
        assertTrue(usuario.getDietas().isEmpty());
    }

    @Test
    void rechazaUnaDietaQueNoExiste() {
        assertThrows(RecursoNoEncontradoException.class, () -> servicio.establecerDietas(1, List.of("INVENTADA")));
    }

    @Test
    void sePuedenLeerLasDietasDelUsuario() {
        servicio.establecerDietas(1, List.of("VEGANA"));
        assertEquals(1, servicio.dietasDe(1).size());
        assertEquals("VEGANA", servicio.dietasDe(1).get(0).getCodigo());
    }
}
