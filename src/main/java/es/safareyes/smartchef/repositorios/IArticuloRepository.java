package es.safareyes.smartchef.repositorios;

import es.safareyes.smartchef.modelos.Articulo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface IArticuloRepository extends JpaRepository<Articulo, Integer> {

    Optional<Articulo> findBySlug(String slug);

    List<Articulo> findByPublicadoTrueOrderByPublicadoEnDesc();
}
