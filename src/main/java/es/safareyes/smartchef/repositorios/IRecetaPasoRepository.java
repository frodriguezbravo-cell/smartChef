package es.safareyes.smartchef.repositorios;

import es.safareyes.smartchef.modelos.RecetaPaso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface IRecetaPasoRepository extends JpaRepository<RecetaPaso, Integer> {

    List<RecetaPaso> findByRecetaIdOrderByNumeroAsc(Integer idReceta);
}
