package es.safareyes.smartchef.servicios;

import es.safareyes.smartchef.modelos.Usuario;
import es.safareyes.smartchef.modelos.UsuarioAlergeno;
import es.safareyes.smartchef.modelos.catalogos.Alergeno;
import es.safareyes.smartchef.modelos.catalogos.Dieta;
import es.safareyes.smartchef.modelos.catalogos.Gravedad;
import es.safareyes.smartchef.repositorios.IAlergenoRepository;
import es.safareyes.smartchef.repositorios.IDietaRepository;
import es.safareyes.smartchef.repositorios.IGravedadRepository;
import es.safareyes.smartchef.repositorios.IUsuarioAlergenoRepository;
import es.safareyes.smartchef.repositorios.IUsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Alergias, intolerancias y dietas del usuario. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PerfilUsuarioService {

    private static final String GRAVEDAD_POR_DEFECTO = "ALERGIA_LEVE";

    private final IUsuarioRepository usuarioRepository;
    private final IUsuarioAlergenoRepository usuarioAlergenoRepository;
    private final IAlergenoRepository alergenoRepository;
    private final IGravedadRepository gravedadRepository;
    private final IDietaRepository dietaRepository;

    public List<UsuarioAlergeno> alergiasDe(Integer idUsuario) {
        return usuarioAlergenoRepository.findByUsuarioId(idUsuario);
    }

    /**
     * Registra una alergia o intolerancia. Si el usuario ya la tenía, actualiza su gravedad y sus notas.
     *
     * @param codigoGravedad INTOLERANCIA, ALERGIA_LEVE o ALERGIA_GRAVE (null = ALERGIA_LEVE)
     */
    @Transactional
    public UsuarioAlergeno anadirAlergeno(Integer idUsuario, String codigoAlergeno, String codigoGravedad, String notas) {
        Usuario usuario = Comprobaciones.existe(usuarioRepository.findById(idUsuario), "Usuario", idUsuario);
        Alergeno alergeno = Comprobaciones.existe(alergenoRepository.findByCodigo(codigoAlergeno), "Alérgeno", codigoAlergeno);
        String codigoG = codigoGravedad == null ? GRAVEDAD_POR_DEFECTO : codigoGravedad;
        Gravedad gravedad = Comprobaciones.existe(gravedadRepository.findByCodigo(codigoG), "Gravedad", codigoG);

        UsuarioAlergeno registro = usuarioAlergenoRepository
                .findByUsuarioIdAndAlergenoId(idUsuario, alergeno.getId())
                .orElseGet(() -> {
                    UsuarioAlergeno nuevo = new UsuarioAlergeno();
                    nuevo.setUsuario(usuario);
                    nuevo.setAlergeno(alergeno);
                    return nuevo;
                });
        registro.setGravedad(gravedad);
        registro.setNotas(notas);
        return usuarioAlergenoRepository.save(registro);
    }

    @Transactional
    public void quitarAlergeno(Integer idUsuario, String codigoAlergeno) {
        Alergeno alergeno = Comprobaciones.existe(alergenoRepository.findByCodigo(codigoAlergeno), "Alérgeno", codigoAlergeno);
        usuarioAlergenoRepository.findByUsuarioIdAndAlergenoId(idUsuario, alergeno.getId())
                .ifPresent(usuarioAlergenoRepository::delete);
    }

    /** Sustituye las dietas del usuario por las indicadas (lista vacía = sin dietas). */
    @Transactional
    public Usuario establecerDietas(Integer idUsuario, List<String> codigosDieta) {
        Usuario usuario = Comprobaciones.existe(usuarioRepository.findById(idUsuario), "Usuario", idUsuario);
        Set<String> sinRepetidos = new LinkedHashSet<>(codigosDieta);
        List<Dieta> dietas = sinRepetidos.stream()
                .map(codigo -> Comprobaciones.existe(dietaRepository.findByCodigo(codigo), "Dieta", codigo))
                .toList();
        usuario.getDietas().clear();
        usuario.getDietas().addAll(dietas);
        return usuarioRepository.save(usuario);
    }

    public List<Dieta> dietasDe(Integer idUsuario) {
        Usuario usuario = Comprobaciones.existe(usuarioRepository.findById(idUsuario), "Usuario", idUsuario);
        return List.copyOf(usuario.getDietas());
    }
}
