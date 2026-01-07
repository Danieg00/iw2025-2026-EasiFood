package com.easifood.app.repository;

import com.easifood.app.model.PedidoProducto;
import com.easifood.app.model.Pedido;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PedidoProductoRepository extends JpaRepository<PedidoProducto, Long> {

    List<PedidoProducto> findByPedido(Pedido pedido);
}
