package es.safareyes.smartchef.modelos.catalogos;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;

/** Opciones iniciales: 1=GLUTEN, 2=CRUSTACEOS, 3=HUEVOS, 4=PESCADO, 5=CACAHUETES, 6=SOJA, 7=LACTEOS, 8=FRUTOS_CASCARA, 9=APIO, 10=MOSTAZA, 11=SESAMO, 12=SULFITOS, 13=ALTRAMUCES, 14=MOLUSCOS */
@Entity
@Table(name = "cat_alergeno")
@NoArgsConstructor
public class Alergeno extends Catalogo {
}
