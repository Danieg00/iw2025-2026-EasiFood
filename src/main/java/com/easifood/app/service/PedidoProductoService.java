package com.easifood.app.service;

import com.easifood.app.model.Pedido;
import com.easifood.app.repository.PedidoRepository;
import com.easifood.app.model.PedidoProducto;
import com.easifood.app.repository.PedidoProductoRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class PedidoProductoService {

    private final PedidoProductoRepository repository;

    public PedidoProductoService(PedidoProductoRepository repository) {
        this.repository = repository;
    }

    public List<PedidoProducto> productosDelPedido(Pedido pedido) {
        return repository.findByPedido(pedido);
    }
}