package es.safareyes.smartchef.modelos.catalogos;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;

/** Opciones iniciales: 1=AMERICA_SUR, 2=AMERICA_CENTRAL, 3=AMERICA_NORTE, 4=AFRICA, 5=ASIA, 6=OCEANIA, 7=EUROPA, 8=INTERNACIONAL */
@Entity
@Table(name = "cat_continente")
@NoArgsConstructor
public class Continente extends Catalogo {
}
