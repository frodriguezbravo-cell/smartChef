package es.safareyes.smartchef.repositorios;

import es.safareyes.smartchef.modelos.IngredienteNombreAlternativo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface IIngredienteNombreAlternativoRepository extends JpaRepository<IngredienteNombreAlternativo, Integer> {

    List<IngredienteNombreAlternativo> findByNombreContainingIgnoreCase(String texto);
}
