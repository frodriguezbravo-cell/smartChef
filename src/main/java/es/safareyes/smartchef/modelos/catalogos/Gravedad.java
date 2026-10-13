package es.safareyes.smartchef.modelos.catalogos;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;

/** Opciones iniciales: 1=INTOLERANCIA, 2=ALERGIA_LEVE, 3=ALERGIA_GRAVE */
@Entity
@Table(name = "cat_gravedad")
@NoArgsConstructor
public class Gravedad extends Catalogo {
}
