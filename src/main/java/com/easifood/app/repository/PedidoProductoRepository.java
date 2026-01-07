package com.easifood.app.repository;

import com.easifood.app.model.PedidoProducto;
import com.easifood.app.model.Pedido;
//import com.easifood.app.model.Empleado;
//import com.easifood.app.model.Restaurante;
//import com.easifood.app.model.Cliente;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface PedidoProductoRepository
        extends JpaRepository<PedidoProducto, Long> {

    List<PedidoProducto> findByPedido(Pedido pedido);
}