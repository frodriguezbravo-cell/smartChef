package es.safareyes.smartchef.modelos.catalogos;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;

/** Opciones iniciales: 1=FAVORITA, 2=PARA_COCINAR */
@Entity
@Table(name = "cat_lista_guardada")
@NoArgsConstructor
public class ListaGuardada extends Catalogo {
}
