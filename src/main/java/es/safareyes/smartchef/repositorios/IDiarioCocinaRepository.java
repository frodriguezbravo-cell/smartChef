package es.safareyes.smartchef.repositorios;

import es.safareyes.smartchef.modelos.DiarioCocina;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface IDiarioCocinaRepository extends JpaRepository<DiarioCocina, Integer> {

    List<DiarioCocina> findByUsuarioIdOrderByFechaDesc(Integer idUsuario);

    List<DiarioCocina> findByRecetaId(Integer idReceta);
}
