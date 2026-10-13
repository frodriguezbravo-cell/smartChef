package es.safareyes.smartchef.repositorios;

import es.safareyes.smartchef.modelos.ListaCompraItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface IListaCompraItemRepository extends JpaRepository<ListaCompraItem, Integer> {

    List<ListaCompraItem> findByListaId(Integer idLista);

    List<ListaCompraItem> findByListaIdAndCompradoFalse(Integer idLista);
}
