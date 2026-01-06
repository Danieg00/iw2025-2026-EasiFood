package com.easifood.app.repository;

import com.easifood.app.model.Pedido;
import com.easifood.app.model.Empleado;
import com.easifood.app.model.Restaurante;
import com.easifood.app.model.Cliente;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.time.LocalDateTime;
import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    // Pedidos de un restaurante
    List<Pedido> findByRestaurante(Restaurante restaurante);

    // Pedidos asignados a un empleado (repartidor)
    List<Pedido> findByEmpleado(Empleado empleado);

    // Pedidos de un cliente
    List<Pedido> findByCliente(Cliente cliente);

    // Pedidos por estado
    List<Pedido> findByEstado(String estado);

    List<Pedido> findByRestauranteAndFechaCreacionBetween(
            Restaurante restaurante,
            LocalDateTime inicio,
            LocalDateTime fin
    );

    List<Pedido> findByClienteOrderByFechaCreacionDesc(Cliente cliente);
    Optional<Pedido> findByIdAndCliente(Long id, Cliente cliente);
}