package es.safareyes.smartchef.repositorios;

import es.safareyes.smartchef.dtos.IngredientesPorCategoria;
import es.safareyes.smartchef.modelos.CategoriaIngrediente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ICategoriaIngredienteRepository extends JpaRepository<CategoriaIngrediente, Integer> {

    /**
     * JPQL con constructor de DTO: el resultado se convierte directamente en IngredientesPorCategoria.
     * Informe de cuántos ingredientes hay en cada categoría.
     */
    @Query("select new es.safareyes.smartchef.dtos.IngredientesPorCategoria(c.nombre, count(i)) " +
            "from Ingrediente i join i.categoria c " +
            "group by c.nombre, c.orden order by c.orden")
    List<IngredientesPorCategoria> contarIngredientesPorCategoria();
}
