package es.safareyes.smartchef.repositorios;

import es.safareyes.smartchef.modelos.DispositivoPush;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface IDispositivoPushRepository extends JpaRepository<DispositivoPush, Integer> {

    List<DispositivoPush> findByUsuarioIdAndActivoTrue(Integer idUsuario);

    Optional<DispositivoPush> findByTokenPush(String tokenPush);
}
