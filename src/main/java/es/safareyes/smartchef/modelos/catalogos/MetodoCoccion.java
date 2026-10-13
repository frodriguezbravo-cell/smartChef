package es.safareyes.smartchef.modelos.catalogos;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;

/** Opciones iniciales: 1=CRUDO, 2=HERVIDO, 3=SARTEN, 4=HORNO, 5=VAPOR, 6=GUISADO, 7=PARRILLA, 8=MICROONDAS, 9=FREIDORA_AIRE */
@Entity
@Table(name = "cat_metodo_coccion")
@NoArgsConstructor
public class MetodoCoccion extends Catalogo {
}
