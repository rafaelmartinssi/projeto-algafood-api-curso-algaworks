package com.algaworks.algafood.api.controller;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.algaworks.algafood.api.assembler.FormaPagamentoAssembler;
import com.algaworks.algafood.api.disassembler.FormaPagamentoDisassembler;
import com.algaworks.algafood.api.model.FormaPagamentoModel;
import com.algaworks.algafood.api.model.input.FormaPagamentoInput;
import com.algaworks.algafood.core.security.CheckSecurity;
import com.algaworks.algafood.domain.model.FormaPagamento;
import com.algaworks.algafood.domain.repository.FormaPagamentoRepository;
import com.algaworks.algafood.domain.service.CadastroFormaPagamentoService;

@RestController
@RequestMapping("/formas-pagamento")
public class FormaPagamentoController {
	
	@Autowired
	private FormaPagamentoRepository formaPagamentoRepository;
	@Autowired
	private CadastroFormaPagamentoService cadastroFormaPagamento;
	@Autowired
	private FormaPagamentoAssembler formaPagamentoAssembler;
	@Autowired
	private FormaPagamentoDisassembler formaPagamentoDisassembler;
	
	@CheckSecurity.FormasPagamento.PodeConsultar
	@GetMapping
	public ResponseEntity<List<FormaPagamentoModel>> listar(){
		
		List<FormaPagamento> formaPagamentos = formaPagamentoRepository.findAll();
		
		List<FormaPagamentoModel> formasPagamentosModel = formaPagamentoAssembler
				.toCollectModel(formaPagamentos);
		
		return ResponseEntity.ok()
				.cacheControl(CacheControl.maxAge(10, TimeUnit.SECONDS).cachePublic())
				.body(formasPagamentosModel);
	}
	
	@CheckSecurity.FormasPagamento.PodeConsultar
	@GetMapping("/{formaPagamentoId}")
	public FormaPagamentoModel buscar(@PathVariable Long formaPagamentoId) {
		FormaPagamento formaPagamento = cadastroFormaPagamento.buscarOuFalhar(formaPagamentoId);
		return formaPagamentoAssembler.toModel(formaPagamento);
	}
	
	@CheckSecurity.FormasPagamento.PodeEditar
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public FormaPagamentoModel salvar(@RequestBody @Valid FormaPagamentoInput formaPagamentoInput) {
		FormaPagamento formaPagamento = formaPagamentoDisassembler.toDomainObject(formaPagamentoInput);
		formaPagamento = cadastroFormaPagamento.salvar(formaPagamento);
		return formaPagamentoAssembler.toModel(formaPagamento);
	}
	
	@CheckSecurity.FormasPagamento.PodeEditar
	@PutMapping("/{formaPagamentoId}")
	public FormaPagamentoModel atualizar(@PathVariable Long formaPagamentoId,
			@RequestBody @Valid FormaPagamentoInput formaPagamentoInput) {
		FormaPagamento formaPagamento = cadastroFormaPagamento.buscarOuFalhar(formaPagamentoId);
		formaPagamentoDisassembler.copyToDomainObject(formaPagamentoInput, formaPagamento);
		formaPagamento = cadastroFormaPagamento.salvar(formaPagamento);
		return formaPagamentoAssembler.toModel(formaPagamento);
	}
	
	@CheckSecurity.FormasPagamento.PodeEditar
	@DeleteMapping("/{formaPagamentoId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void remover(@PathVariable Long formaPagamentoId) {
		cadastroFormaPagamento.remover(formaPagamentoId);
	}

}
