package es.safareyes.smartchef.repositorios;

import es.safareyes.smartchef.modelos.RecetaIngrediente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface IRecetaIngredienteRepository extends JpaRepository<RecetaIngrediente, Integer> {

    List<RecetaIngrediente> findByRecetaIdOrderByOrdenAsc(Integer idReceta);
}
