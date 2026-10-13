package es.safareyes.smartchef.modelos.catalogos;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;

/** Opciones iniciales: 1=G, 2=KG, 3=ML, 4=L, 5=UD, 6=CUCHARADA, 7=CUCHARADITA, 8=TAZA, 9=PIZCA, 10=AL_GUSTO */
@Entity
@Table(name = "cat_unidad_medida")
@NoArgsConstructor
public class UnidadMedida extends Catalogo {
}
