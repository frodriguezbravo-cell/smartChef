package es.safareyes.smartchef.modelos.catalogos;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;

/** Opciones iniciales: 1=DESAYUNO, 2=ENTRANTE, 3=ENSALADA, 4=SOPA_CREMA, 5=PRINCIPAL, 6=POSTRE, 7=MERIENDA, 8=SALSA, 9=BEBIDA */
@Entity
@Table(name = "cat_tipo_plato")
@NoArgsConstructor
public class TipoPlato extends Catalogo {
}
