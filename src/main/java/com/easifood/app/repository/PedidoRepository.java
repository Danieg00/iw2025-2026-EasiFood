package com.easifood.app.repository;

import com.easifood.app.model.Cliente;
import com.easifood.app.model.Empleado;
import com.easifood.app.model.Pedido;
import com.easifood.app.model.Restaurante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    List<Pedido> findByEmpleado(Empleado empleado);

    List<Pedido> findByClienteOrderByFechaCreacionDesc(Cliente cliente);

    Optional<Pedido> findByIdAndCliente(Long id, Cliente cliente);

    List<Pedido> findByRestauranteAndFechaCreacionBetween(Restaurante restaurante,
                                                          LocalDateTime inicio,
                                                          LocalDateTime fin);

    // ✅ IMPORTANTE: para el detalle -> trae lineas + producto ya inicializados
    @Query("""
        select distinct p
        from Pedido p
        left join fetch p.lineas l
        left join fetch l.producto pr
        where p.id = :pedidoId
          and p.cliente = :cliente
    """)
    Optional<Pedido> findByIdAndClienteWithLineas(@Param("pedidoId") Long pedidoId,
                                                  @Param("cliente") Cliente cliente);

    // ✅ Útil para updates (si quieres devolver el pedido ya "listo" también)
    @Query("""
        select distinct p
        from Pedido p
        left join fetch p.lineas l
        left join fetch l.producto pr
        where p.id = :pedidoId
    """)
    Optional<Pedido> findByIdWithLineas(@Param("pedidoId") Long pedidoId);
}
