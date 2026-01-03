package com.easifood.app.service;

import com.easifood.app.model.Producto;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
public class CarritoService {

    private static final String SESSION_KEY = "EASIFOOD_CARRITOS_POR_REST";

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
    private Map<Long, Map<Long, Item>> all() {
        VaadinSession session = VaadinSession.getCurrent();
        Map<Long, Map<Long, Item>> data = (Map<Long, Map<Long, Item>>) session.getAttribute(SESSION_KEY);
        if (data == null) {
            data = new LinkedHashMap<>();
            session.setAttribute(SESSION_KEY, data);
        }
        return data;
    }

    private Map<Long, Item> cart(Long restauranteId) {
        if (restauranteId == null) throw new IllegalArgumentException("restauranteId null");
        Map<Long, Map<Long, Item>> data = all();
        return data.computeIfAbsent(restauranteId, k -> new LinkedHashMap<>());
    }

    public void add(Long restauranteId, Producto p) {
        if (p == null || p.getId() == null) return;

        Map<Long, Item> m = cart(restauranteId);
        Item it = m.get(p.getId());
        if (it == null) m.put(p.getId(), new Item(p, 1));
        else it.setCantidad(it.getCantidad() + 1);
    }

    public void dec(Long restauranteId, Producto p) {
        if (p == null || p.getId() == null) return;

        Map<Long, Item> m = cart(restauranteId);
        Item it = m.get(p.getId());
        if (it == null) return;

        int next = it.getCantidad() - 1;
        if (next <= 0) m.remove(p.getId());
        else it.setCantidad(next);
    }

    public void remove(Long restauranteId, Long productoId) {
        cart(restauranteId).remove(productoId);
    }

    public void clear(Long restauranteId) {
        cart(restauranteId).clear();
    }

    public List<Item> items(Long restauranteId) {
        return new ArrayList<>(cart(restauranteId).values());
    }

    public int totalUnidades(Long restauranteId) {
        int sum = 0;
        for (Item it : cart(restauranteId).values()) sum += it.getCantidad();
        return sum;
    }

    public BigDecimal totalPrecio(Long restauranteId) {
        BigDecimal total = BigDecimal.ZERO;
        for (Item it : cart(restauranteId).values()) {
            BigDecimal precio = it.getProducto().getPrecio();
            if (precio != null) {
                total = total.add(precio.multiply(BigDecimal.valueOf(it.getCantidad())));
            }
        }
        return total;
    }
}
