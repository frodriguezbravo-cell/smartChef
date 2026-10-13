package es.safareyes.smartchef.repositorios;

import es.safareyes.smartchef.modelos.DiarioFoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface IDiarioFotoRepository extends JpaRepository<DiarioFoto, Integer> {

    List<DiarioFoto> findByDiarioId(Integer idDiario);
}
