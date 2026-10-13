package es.safareyes.smartchef.modelos.catalogos;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;

/** Opciones iniciales: 1=ALACENA, 2=NEVERA, 3=CONGELADOR */
@Entity
@Table(name = "cat_ubicacion_despensa")
@NoArgsConstructor
public class UbicacionDespensa extends Catalogo {
}
