package es.safareyes.smartchef.pruebas;

import es.safareyes.smartchef.modelos.Usuario;
import es.safareyes.smartchef.modelos.catalogos.Catalogo;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * Repositorios "de mentira" para probar los servicios sin base de datos y sin Mockito.
 * Cada método que no se configura devuelve algo vacío (Optional.empty, lista vacía, false)
 * y save(x) devuelve el mismo objeto que recibe.
 */
public final class Repos {

    private Repos() {
    }

    /** Alias corto para configurar métodos: metodos().put("findById", a -> Optional.of(x)). */
    public static Map<String, Function<Object[], Object>> metodos() {
        return new HashMap<>();
    }

    @SuppressWarnings("unchecked")
    public static <T> T fake(Class<T> repositorio, Map<String, Function<Object[], Object>> metodos) {
        return (T) Proxy.newProxyInstance(repositorio.getClassLoader(), new Class<?>[]{repositorio}, (proxy, metodo, args) -> {
            Object[] argumentos = args == null ? new Object[0] : args;
            Function<Object[], Object> configurado = metodos.get(metodo.getName());
            if (configurado != null) {
                return configurado.apply(argumentos);
            }
            if (metodo.getName().equals("save")) {
                return argumentos[0];
            }
            Class<?> tipo = metodo.getReturnType();
            if (tipo == Optional.class) {
                return Optional.empty();
            }
            if (tipo == List.class) {
                return List.of();
            }
            if (tipo == boolean.class) {
                return false;
            }
            return null;
        });
    }

    /** Un repositorio sin configurar. */
    public static <T> T fake(Class<T> repositorio) {
        return fake(repositorio, metodos());
    }

    /** Rellena una opción de catálogo (Dieta, Alergeno, TipoComida...). */
    public static <T extends Catalogo> T opcion(T catalogo, int id, String codigo, String nombre) {
        catalogo.setId((short) id);
        catalogo.setCodigo(codigo);
        catalogo.setNombre(nombre);
        return catalogo;
    }

    public static Usuario usuario(int id) {
        Usuario u = new Usuario();
        u.setId(id);
        return u;
    }
}
