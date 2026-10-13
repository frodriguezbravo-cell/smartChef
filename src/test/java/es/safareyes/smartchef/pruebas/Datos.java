package es.safareyes.smartchef.pruebas;

import es.safareyes.smartchef.modelos.DespensaItem;
import es.safareyes.smartchef.modelos.Ingrediente;
import es.safareyes.smartchef.modelos.Receta;
import es.safareyes.smartchef.modelos.RecetaIngrediente;
import es.safareyes.smartchef.modelos.catalogos.Dieta;
import es.safareyes.smartchef.modelos.catalogos.UnidadMedida;

import java.math.BigDecimal;

/** Construye objetos en memoria para las pruebas que no necesitan base de datos. */
public final class Datos {

    private Datos() {
    }

    public static BigDecimal bd(String valor) {
        return new BigDecimal(valor);
    }

    public static UnidadMedida unidad(int id, String codigo, String nombre) {
        UnidadMedida u = new UnidadMedida();
        u.setId((short) id);
        u.setCodigo(codigo);
        u.setNombre(nombre);
        return u;
    }

    public static Dieta dieta(String codigo) {
        Dieta d = new Dieta();
        d.setCodigo(codigo);
        return d;
    }

    /** Valores nutricionales por 100 g: kcal, proteínas, hidratos, azúcares, grasas, saturadas, fibra, sal. */
    public static Ingrediente ingrediente(int id, String nombre, String kcal, String prot, String hid,
                                          String azu, String gra, String sat, String fib, String sal) {
        Ingrediente i = new Ingrediente();
        i.setId(id);
        i.setNombre(nombre);
        i.setCaloriasKcal(bd(kcal));
        i.setProteinasG(bd(prot));
        i.setHidratosG(bd(hid));
        i.setAzucaresG(bd(azu));
        i.setGrasasG(bd(gra));
        i.setGrasasSaturadasG(bd(sat));
        i.setFibraG(bd(fib));
        i.setSalG(bd(sal));
        return i;
    }

    public static Receta receta(String titulo, int raciones, String... dietas) {
        Receta r = new Receta();
        r.setTitulo(titulo);
        r.setRaciones((short) raciones);
        for (String d : dietas) {
            r.getDietas().add(dieta(d));
        }
        return r;
    }

    /** Añade una línea a la receta. cantidad o gramos pueden ser null. */
    public static RecetaIngrediente linea(Receta receta, Ingrediente ingrediente, String cantidad,
                                          UnidadMedida unidad, String gramos, boolean opcional) {
        RecetaIngrediente l = new RecetaIngrediente();
        l.setReceta(receta);
        l.setIngrediente(ingrediente);
        l.setCantidad(cantidad == null ? null : bd(cantidad));
        l.setUnidad(unidad);
        l.setCantidadGramos(gramos == null ? null : bd(gramos));
        l.setEsOpcional(opcional);
        l.setOrden((short) receta.getIngredientes().size());
        receta.getIngredientes().add(l);
        return l;
    }

    public static DespensaItem stock(Ingrediente ingrediente, String cantidad, UnidadMedida unidad) {
        DespensaItem d = new DespensaItem();
        d.setIngrediente(ingrediente);
        d.setCantidad(bd(cantidad));
        d.setUnidad(unidad);
        return d;
    }

    /** Los espaguetis al pomodoro del script SQL: 2 raciones, parmesano opcional. */
    public static Receta espaguetisAlPomodoro() {
        Receta r = receta("Espaguetis al pomodoro", 2, "VEGETARIANA");
        UnidadMedida g = unidad(1, "G", "gramos (g)");
        UnidadMedida ud = unidad(5, "UD", "unidades");
        UnidadMedida cda = unidad(6, "CUCHARADA", "cucharadas");
        linea(r, ingrediente(1, "Espaguetis", "359", "12.5", "72", "3.5", "1.5", "0.3", "3", "0.01"), "200", g, "200", false);
        linea(r, ingrediente(2, "Tomate", "18", "0.9", "3.9", "2.6", "0.2", "0", "1.2", "0.01"), "400", g, "400", false);
        linea(r, ingrediente(3, "Ajo", "149", "6.4", "33.1", "1.0", "0.5", "0.1", "2.1", "0.02"), "2", ud, "8", false);
        linea(r, ingrediente(4, "Aceite de oliva virgen extra", "884", "0", "0", "0", "100", "14", "0", "0"), "2", cda, "18", false);
        linea(r, ingrediente(5, "Queso parmesano", "392", "35.8", "3.2", "0.8", "25.8", "16", "0", "1.6"), "20", g, "20", true);
        return r;
    }
}
