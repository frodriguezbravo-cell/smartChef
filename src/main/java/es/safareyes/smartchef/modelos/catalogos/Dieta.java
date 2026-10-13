package es.safareyes.smartchef.modelos.catalogos;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;

/** Opciones iniciales: 1=OMNIVORA, 2=VEGETARIANA, 3=VEGANA, 4=PESCETARIANA, 5=SIN_GLUTEN, 6=SIN_LACTOSA, 7=KETO, 8=BAJA_CALORIAS, 9=ALTA_PROTEINA */
@Entity
@Table(name = "cat_dieta")
@NoArgsConstructor
public class Dieta extends Catalogo {
}
