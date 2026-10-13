package es.safareyes.smartchef.repositorios;

import es.safareyes.smartchef.modelos.RecetaImagen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface IRecetaImagenRepository extends JpaRepository<RecetaImagen, Integer> {

    List<RecetaImagen> findByRecetaIdOrderByOrdenAsc(Integer idReceta);
}
