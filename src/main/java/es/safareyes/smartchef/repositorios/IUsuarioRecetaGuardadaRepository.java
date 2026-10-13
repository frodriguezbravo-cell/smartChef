package es.safareyes.smartchef.repositorios;

import es.safareyes.smartchef.modelos.UsuarioRecetaGuardada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface IUsuarioRecetaGuardadaRepository extends JpaRepository<UsuarioRecetaGuardada, Integer> {

    List<UsuarioRecetaGuardada> findByUsuarioIdAndListaCodigo(Integer idUsuario, String codigoLista);

    boolean existsByUsuarioIdAndRecetaIdAndListaId(Integer idUsuario, Integer idReceta, Short idLista);
}
