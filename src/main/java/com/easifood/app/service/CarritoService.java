package com.easifood.app.service;

import com.easifood.app.model.Producto;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
public class CarritoService {

    private static final String SESSION_KEY = "EASIFOOD_CARRITO";
    private static final String REST_KEY = "EASIFOOD_CARRITO_REST";

    public static class Item {
        private final Producto producto;
        private int cantidad;

        public Item(Producto producto, int cantidad) {
            this.producto = producto;
            this.cantidad = cantidad;
        }

        public Producto getProducto() { return producto; }
        public int getCantidad() { return cantidad; }
        public void setCantidad(int cantidad) { this.cantidad = Math.max(1, cantidad); }
    }

    @SuppressWarnings("unchecked")
    private Map<Long, Item> map() {
        VaadinSession session = VaadinSession.getCurrent();
        Map<Long, Item> m = (Map<Long, Item>) session.getAttribute(SESSION_KEY);
        if (m == null) {
            m = new LinkedHashMap<>();
            session.setAttribute(SESSION_KEY, m);
        }
        return m;
    }

    public Long getRestauranteId() {
        return (Long) VaadinSession.getCurrent().getAttribute(REST_KEY);
    }

    private void setRestauranteId(Long id) {
        VaadinSession.getCurrent().setAttribute(REST_KEY, id);
    }

    private void clearRestauranteId() {
        VaadinSession.getCurrent().setAttribute(REST_KEY, null);
    }

    // ✅ Nuevo: añadir exige restauranteId
    public void add(Producto p, Long restauranteId) {
        if (p == null || p.getId() == null) return;
        if (restauranteId == null) return;

        Long currentRest = getRestauranteId();
        if (currentRest == null) {
            setRestauranteId(restauranteId);
        } else if (!currentRest.equals(restauranteId)) {
            throw new IllegalStateException("CARRITO_OTRO_RESTAURANTE");
        }

        Map<Long, Item> m = map();
        Item it = m.get(p.getId());
        if (it == null) m.put(p.getId(), new Item(p, 1));
        else it.setCantidad(it.getCantidad() + 1);
    }

    // Para botones +/- desde el carrito (no hace falta restauranteId porque ya está fijado)
    public void add(Producto p) {
        Long rest = getRestauranteId();
        if (rest == null) return;
        add(p, rest);
    }

    public void dec(Producto p) {
        if (p == null || p.getId() == null) return;
        Map<Long, Item> m = map();
        Item it = m.get(p.getId());
        if (it == null) return;

        int next = it.getCantidad() - 1;
        if (next <= 0) m.remove(p.getId());
        else it.setCantidad(next);

        if (m.isEmpty()) clearRestauranteId();
    }

    public void remove(Long productoId) {
        map().remove(productoId);
        if (map().isEmpty()) clearRestauranteId();
    }

    public void clear() {
        map().clear();
        clearRestauranteId();
    }

    public List<Item> items() {
        return new ArrayList<>(map().values());
    }

    public int totalUnidades() {
        int sum = 0;
        for (Item it : map().values()) sum += it.getCantidad();
        return sum;
    }

    public BigDecimal totalPrecio() {
        BigDecimal total = BigDecimal.ZERO;
        for (Item it : map().values()) {
            BigDecimal precio = it.getProducto().getPrecio();
            if (precio != null) {
                total = total.add(precio.multiply(BigDecimal.valueOf(it.getCantidad())));
            }
        }
        return total;
    }
}
