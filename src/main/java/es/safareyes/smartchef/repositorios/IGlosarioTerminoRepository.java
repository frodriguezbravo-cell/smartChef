package es.safareyes.smartchef.repositorios;

import es.safareyes.smartchef.modelos.GlosarioTermino;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface IGlosarioTerminoRepository extends JpaRepository<GlosarioTermino, Integer> {

    List<GlosarioTermino> findByTerminoContainingIgnoreCase(String texto);
}
