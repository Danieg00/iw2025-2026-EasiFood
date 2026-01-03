package com.easifood.app.service;

import com.easifood.app.model.Empleado;
import com.easifood.app.model.Pedido;
import com.easifood.app.repository.PedidoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;

    public PedidoService(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    public List<Pedido> pedidosDelEmpleado(Empleado empleado) {
        return pedidoRepository.findByEmpleado(empleado);
    }

    public Pedido guardar(Pedido pedido) {
        return pedidoRepository.save(pedido);
    }
}