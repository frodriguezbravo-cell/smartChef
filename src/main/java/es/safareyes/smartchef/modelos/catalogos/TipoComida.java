package es.safareyes.smartchef.modelos.catalogos;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;

/** Opciones iniciales: 1=DESAYUNO, 2=ALMUERZO, 3=COMIDA, 4=MERIENDA, 5=CENA */
@Entity
@Table(name = "cat_tipo_comida")
@NoArgsConstructor
public class TipoComida extends Catalogo {
}
