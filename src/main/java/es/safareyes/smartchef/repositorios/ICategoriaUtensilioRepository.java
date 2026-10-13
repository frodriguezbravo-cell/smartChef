package es.safareyes.smartchef.repositorios;

import es.safareyes.smartchef.modelos.CategoriaUtensilio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ICategoriaUtensilioRepository extends JpaRepository<CategoriaUtensilio, Integer> {

    /** Categorías de primer nivel (sin padre). */
    List<CategoriaUtensilio> findByCategoriaPadreIsNullOrderByOrdenAsc();

    List<CategoriaUtensilio> findByCategoriaPadreIdOrderByOrdenAsc(Integer idPadre);
}
