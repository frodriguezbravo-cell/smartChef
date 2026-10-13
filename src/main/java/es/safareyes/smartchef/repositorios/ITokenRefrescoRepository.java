package es.safareyes.smartchef.repositorios;

import es.safareyes.smartchef.modelos.TokenRefresco;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ITokenRefrescoRepository extends JpaRepository<TokenRefresco, Integer> {

    Optional<TokenRefresco> findByTokenHash(String tokenHash);

    List<TokenRefresco> findByUsuarioIdAndRevocadoFalse(Integer idUsuario);
}
