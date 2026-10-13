package es.safareyes.smartchef.repositorios;

import es.safareyes.smartchef.modelos.Origen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface IOrigenRepository extends JpaRepository<Origen, Integer> {

    List<Origen> findByContinenteCodigo(String codigoContinente);
}
