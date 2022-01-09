package com.algaworks.algafood.domain.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;

import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.domain.AbstractAggregateRoot;

import com.algaworks.algafood.domain.event.PedidoCanceladoEvent;
import com.algaworks.algafood.domain.event.PedidoConfirmadoEvent;
import com.algaworks.algafood.domain.exception.NegocioException;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class Pedido extends AbstractAggregateRoot<Pedido>{
	
	@Id
	@EqualsAndHashCode.Include
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private String codigo;
	
	private BigDecimal subtotal;
	private BigDecimal taxaFrete;
	private BigDecimal valorTotal;
	
	@Embedded
	private Endereco enderecoEntrega;
	
	@Enumerated(EnumType.STRING)
	private StatusPedido status = StatusPedido.CRIADO;
	
	@Column(columnDefinition = "datetime")
	@CreationTimestamp
	private OffsetDateTime dataCriacao;
	
	@Column(columnDefinition = "datetime")
	private OffsetDateTime dataConfirmacao;
	@Column(columnDefinition = "datetime")
	private OffsetDateTime dataCancelamento;
	@Column(columnDefinition = "datetime")
	private OffsetDateTime dataEntrega;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	private FormaPagamento formaPagamento;
	
	@ManyToOne
	@JoinColumn(nullable = false)
	private Restaurante restaurante;
	
	@ManyToOne
	@JoinColumn(name = "usuario_cliente_id", nullable = false)
	private Usuario cliente;
	
	@OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL)
	private List<ItemPedido> itens = new ArrayList<>();
	
	public void calcularValorTotal() {
	    getItens().forEach(ItemPedido::calcularPrecoTotal);
	    
	    this.subtotal = getItens().stream()
	        .map(item -> item.getPrecoTotal())
	        .reduce(BigDecimal.ZERO, BigDecimal::add);
	    
	    this.valorTotal = this.subtotal.add(this.taxaFrete);
	}

	public void definirFrete() {
	    setTaxaFrete(getRestaurante().getTaxaFrete());
	}

	public void atribuirPedidoAosItens() {
	    getItens().forEach(item -> item.setPedido(this));
	}
	
	public void confirmar() {
		if(!getStatus().equals(StatusPedido.CRIADO)) {
			throw new NegocioException(
					String.format("Status do pedido %s não pode ser alterado de %s para %s", 
							getCodigo(), getStatus().getDescricao(), StatusPedido.CONFIRMADO.getDescricao()));
		}
		setStatus(StatusPedido.CONFIRMADO);
		setDataConfirmacao(OffsetDateTime.now());
		
		registerEvent(new PedidoConfirmadoEvent(this));
	}
	public void entregar() {
		if(!getStatus().equals(StatusPedido.CONFIRMADO)) {
			throw new NegocioException(
					String.format("Status do pedido %s não pode ser alterado de %s para %s", 
							getCodigo(), getStatus().getDescricao(), StatusPedido.ENTREGUE.getDescricao()));
		}
		setStatus(StatusPedido.ENTREGUE);
		setDataEntrega(OffsetDateTime.now());
	}
	public void cancelar() {
		if(!getStatus().equals(StatusPedido.CRIADO)) {
			throw new NegocioException(
					String.format("Status do pedido %s não pode ser alterado de %s para %s", 
							getCodigo(), getStatus().getDescricao(), StatusPedido.CANCELADO.getDescricao()));
		}
		setStatus(StatusPedido.CANCELADO);
		setDataCancelamento(OffsetDateTime.now());
		
		registerEvent(new PedidoCanceladoEvent(this));
	}
	
	@PrePersist
	private void gerarCodigo() {
		setCodigo(UUID.randomUUID().toString());
	} 
}
