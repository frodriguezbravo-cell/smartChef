package es.safareyes.smartchef.modelos.catalogos;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;

/** Opciones iniciales: 1=ESPANOLA, 2=MEDITERRANEA, 3=ITALIANA, 4=MEXICANA, 5=ASIATICA, 6=ARABE, 7=INTERNACIONAL */
@Entity
@Table(name = "cat_tipo_cocina")
@NoArgsConstructor
public class TipoCocina extends Catalogo {
}
