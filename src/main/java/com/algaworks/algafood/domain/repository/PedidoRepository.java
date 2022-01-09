package com.algaworks.algafood.domain.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.algaworks.algafood.domain.model.Pedido;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long>,
	JpaSpecificationExecutor<Pedido>{
	
	Optional<Pedido> findByCodigo(String codigo);
	
	@Query("    select case when count(1) > 0 then true else false end from Pedido ped join ped.restaurante rest join rest.responsaveis resp where ped.codigo = :codigoPedido and resp.id = :usuarioId")
	boolean isPedidoGerenciadoPor(String codigoPedido, Long usuarioId);
	
}
