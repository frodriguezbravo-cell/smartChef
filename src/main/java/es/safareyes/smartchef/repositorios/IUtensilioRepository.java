package es.safareyes.smartchef.repositorios;

import es.safareyes.smartchef.modelos.Utensilio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface IUtensilioRepository extends JpaRepository<Utensilio, Integer> {

    List<Utensilio> findByCategoriaId(Integer idCategoria);

    List<Utensilio> findByModeloContainingIgnoreCase(String texto);
}
