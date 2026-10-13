package es.safareyes.smartchef.repositorios;

import es.safareyes.smartchef.modelos.ListaCompra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface IListaCompraRepository extends JpaRepository<ListaCompra, Integer> {

    List<ListaCompra> findByUsuarioIdOrderByCreadoEnDesc(Integer idUsuario);

    List<ListaCompra> findByUsuarioIdAndCompletadaFalse(Integer idUsuario);
}
