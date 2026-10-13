package es.safareyes.smartchef.repositorios;

import es.safareyes.smartchef.modelos.UsuarioUtensilio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface IUsuarioUtensilioRepository extends JpaRepository<UsuarioUtensilio, Integer> {

    List<UsuarioUtensilio> findByUsuarioId(Integer idUsuario);
}
