package es.safareyes.smartchef.servicios;

import es.safareyes.smartchef.excepciones.RecursoNoEncontradoException;
import es.safareyes.smartchef.modelos.catalogos.Catalogo;
import es.safareyes.smartchef.repositorios.IAlergenoRepository;
import es.safareyes.smartchef.repositorios.IDietaRepository;
import es.safareyes.smartchef.repositorios.IGravedadRepository;
import es.safareyes.smartchef.repositorios.IMotivoBajaRepository;
import es.safareyes.smartchef.repositorios.ITipoComidaRepository;
import es.safareyes.smartchef.repositorios.IUbicacionDespensaRepository;
import es.safareyes.smartchef.repositorios.IUnidadMedidaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

/** Opciones de los catálogos (dietas, alérgenos, unidades...) para rellenar los desplegables de la app. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CatalogoService {

    /** Nombres de catálogo que acepta opciones(). */
    public static final List<String> NOMBRES = List.of("dietas", "alergenos", "gravedades", "unidades",
            "tipos-comida", "ubicaciones", "motivos-baja");

    private final IDietaRepository dietaRepository;
    private final IAlergenoRepository alergenoRepository;
    private final IGravedadRepository gravedadRepository;
    private final IUnidadMedidaRepository unidadRepository;
    private final ITipoComidaRepository tipoComidaRepository;
    private final IUbicacionDespensaRepository ubicacionRepository;
    private final IMotivoBajaRepository motivoBajaRepository;

    /** Opciones activas del catálogo, en el orden definido en la base de datos. */
    public List<Catalogo> opciones(String nombre) {
        List<? extends Catalogo> todas = switch (nombre) {
            case "dietas" -> dietaRepository.findAll();
            case "alergenos" -> alergenoRepository.findAll();
            case "gravedades" -> gravedadRepository.findAll();
            case "unidades" -> unidadRepository.findAll();
            case "tipos-comida" -> tipoComidaRepository.findAll();
            case "ubicaciones" -> ubicacionRepository.findAll();
            case "motivos-baja" -> motivoBajaRepository.findAll();
            default -> throw new RecursoNoEncontradoException("Catálogo", nombre);
        };
        return todas.stream()
                .filter(opcion -> Boolean.TRUE.equals(opcion.getActivo()))
                .sorted(Comparator.comparing(Catalogo::getOrden))
                .map(opcion -> (Catalogo) opcion)
                .toList();
    }
}
