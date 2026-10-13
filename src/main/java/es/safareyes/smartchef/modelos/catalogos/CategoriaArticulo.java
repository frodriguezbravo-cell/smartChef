package es.safareyes.smartchef.modelos.catalogos;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;

/** Opciones iniciales: 1=PRIMEROS_PASOS, 2=TECNICAS, 3=CONSERVACION, 4=NUTRICION, 5=ALERGIAS, 6=DIETAS, 7=PLANIFICACION, 8=UTENSILIOS, 9=TRUCOS */
@Entity
@Table(name = "cat_categoria_articulo")
@NoArgsConstructor
public class CategoriaArticulo extends Catalogo {
}
