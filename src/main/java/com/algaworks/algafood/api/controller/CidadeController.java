package com.algaworks.algafood.api.controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.algaworks.algafood.api.ResourceUriHelper;
import com.algaworks.algafood.api.assembler.CidadeModelAssembler;
import com.algaworks.algafood.api.disassembler.CidadeInputDisassembler;
import com.algaworks.algafood.api.model.CidadeModel;
import com.algaworks.algafood.api.model.input.CidadeInput;
import com.algaworks.algafood.core.security.CheckSecurity;
import com.algaworks.algafood.domain.exception.EstadoNaoEncontradaException;
import com.algaworks.algafood.domain.exception.NegocioException;
import com.algaworks.algafood.domain.model.Cidade;
import com.algaworks.algafood.domain.repository.CidadeRepository;
import com.algaworks.algafood.domain.service.CadastroCidadeService;

@RestController
@RequestMapping("/cidades")
public class CidadeController {

	@Autowired
	private CidadeRepository cidadeRepository;
	@Autowired
	private CadastroCidadeService cadastroCidade;
	@Autowired
	private CidadeModelAssembler cidadeModelAssembler;
	@Autowired
	private CidadeInputDisassembler cidadeInputDisassembler;
	
	@CheckSecurity.Cidades.PodeConsultar
	@GetMapping
	public CollectionModel<CidadeModel> listar(){
		List<Cidade> cidades = cidadeRepository.findAll();
		
		return cidadeModelAssembler.toCollectionModel(cidades);			
	}
	
	@CheckSecurity.Cidades.PodeConsultar
	@GetMapping("/{cidadeId}")
	public CidadeModel buscar(@PathVariable Long cidadeId){
		Cidade cidade = cadastroCidade.buscarOuFalhar(cidadeId);
		
		CidadeModel cidadeModel = cidadeModelAssembler.toModel(cidade);
		
		return cidadeModel;
	}
	
	@CheckSecurity.Cidades.PodeEditar
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public CidadeModel adicionar(@RequestBody @Valid CidadeInput cidadeInput){
		try {
			Cidade cidade = cidadeInputDisassembler.toDomainObject(cidadeInput);
			cidade = cadastroCidade.salvar(cidade);
			
			CidadeModel cidadeModel = cidadeModelAssembler.toModel(cidade);
			
			ResourceUriHelper.addUriInResponseHeader(cidadeModel.getId());
			
			return cidadeModel;
		} catch (EstadoNaoEncontradaException e) {
			throw new NegocioException(e.getMessage(), e);
		}
	}
	
	@CheckSecurity.Cidades.PodeEditar
	@PutMapping("/{cidadeId}")
	public CidadeModel atualizar(@RequestBody @Valid CidadeInput cidadeInput, 
			@PathVariable Long cidadeId){

		try {
		Cidade cidadeAtual = cadastroCidade.buscarOuFalhar(cidadeId);
			
		cidadeInputDisassembler.copyToDomainObject(cidadeInput, cidadeAtual);
		
		cidadeAtual = cadastroCidade.salvar(cidadeAtual);
		
		return cidadeModelAssembler.toModel(cidadeAtual);

		} catch (EstadoNaoEncontradaException e) {
			throw new NegocioException(e.getMessage(), e);
		}
	 		

	}
	
	@CheckSecurity.Cidades.PodeEditar
	@DeleteMapping("/{cidadeId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void remover(@PathVariable Long cidadeId){
		cadastroCidade.remover(cidadeId);
	}
	
}
