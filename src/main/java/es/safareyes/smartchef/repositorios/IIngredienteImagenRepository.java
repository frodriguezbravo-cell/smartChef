package es.safareyes.smartchef.repositorios;

import es.safareyes.smartchef.modelos.IngredienteImagen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface IIngredienteImagenRepository extends JpaRepository<IngredienteImagen, Integer> {

    List<IngredienteImagen> findByIngredienteIdOrderByOrdenAsc(Integer idIngrediente);
}
