package com.easifood.app.service;

import com.easifood.app.model.Empleado;
import com.easifood.app.model.Pedido;
import com.easifood.app.model.Restaurante;
import com.easifood.app.repository.PedidoRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;

    public List<Pedido> findAll() {
        return pedidoRepository.findAll();
    }


    public PedidoService(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    public List<Pedido> pedidosDelEmpleado(Empleado empleado) {
        return pedidoRepository.findByEmpleado(empleado);
    }

    public Pedido guardar(Pedido pedido) {
        return pedidoRepository.save(pedido);
    }

    public List<Pedido> obtenerPedidosDeHoy(Restaurante restaurante) {
        LocalDateTime inicioDia = LocalDate.now().atStartOfDay(); // 00:00:00
        LocalDateTime finDia = LocalDate.now().atTime(LocalTime.MAX); // 23:59:59

        return pedidoRepository.findByRestauranteAndFechaCreacionBetween(restaurante, inicioDia, finDia);
    }

    // Calcular total vendido hoy
    public BigDecimal calcularVentasHoy(Restaurante restaurante) {
        List<Pedido> pedidosHoy = obtenerPedidosDeHoy(restaurante);

        // Sumar todos los totales
        return pedidosHoy.stream()
                .map(Pedido::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}