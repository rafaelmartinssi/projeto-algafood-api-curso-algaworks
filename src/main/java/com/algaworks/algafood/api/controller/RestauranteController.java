package com.algaworks.algafood.api.controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
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

import com.algaworks.algafood.api.assembler.RestauranteModelAssembler;
import com.algaworks.algafood.api.disassembler.RestauranteInputDisassembler;
import com.algaworks.algafood.api.model.RestauranteModel;
import com.algaworks.algafood.api.model.input.RestauranteInput;
import com.algaworks.algafood.api.model.view.RestauranteView;
import com.algaworks.algafood.core.security.CheckSecurity;
import com.algaworks.algafood.domain.exception.CidadeNaoEncontradaException;
import com.algaworks.algafood.domain.exception.CozinhaNaoEncontradaException;
import com.algaworks.algafood.domain.exception.NegocioException;
import com.algaworks.algafood.domain.exception.RestauranteNaoEncontradaException;
import com.algaworks.algafood.domain.model.Restaurante;
import com.algaworks.algafood.domain.repository.RestauranteRepository;
import com.algaworks.algafood.domain.service.CadastroRestauranteService;
import com.fasterxml.jackson.annotation.JsonView;

@RestController
@RequestMapping("/restaurantes")
public class RestauranteController {
	
	@Autowired
	private RestauranteRepository restauranteRepository;
	
	@Autowired
	private CadastroRestauranteService cadastroRestaurante;
	
	@Autowired
	private RestauranteModelAssembler restauranteModelAssembler;
	
	@Autowired
	private RestauranteInputDisassembler restauranteInputDisassembler;
	
	@CheckSecurity.Restaurantes.PodeConsultar
	@JsonView(RestauranteView.Resumo.class)
	@GetMapping
	public List<RestauranteModel> listar(){
		return restauranteModelAssembler.toCollectionModel(restauranteRepository.findAll());
	}
	
	@CheckSecurity.Restaurantes.PodeConsultar
	@JsonView(RestauranteView.ApenasNome.class)
	@GetMapping(params = "projecao=apenas-nome")
	public List<RestauranteModel> listarApenasNome(){
		return listar();
	}
	
	@CheckSecurity.Restaurantes.PodeConsultar
	@GetMapping("/{restauranteId}")
	public RestauranteModel buscar(@PathVariable Long restauranteId){
		Restaurante restaurante = cadastroRestaurante.buscarOuFalhar(restauranteId);
		
		return restauranteModelAssembler.toModel(restaurante);
	
	}
	
	@CheckSecurity.Restaurantes.PodeGerenciarCadastro
	@PostMapping
	public RestauranteModel adicionar(@RequestBody @Valid RestauranteInput restauranteInput){
		try {
			Restaurante restaurante = restauranteInputDisassembler.toDomainObject(restauranteInput);
			return restauranteModelAssembler.toModel(cadastroRestaurante.salvar(restaurante));
		} catch (CozinhaNaoEncontradaException | CidadeNaoEncontradaException e) {
			throw new NegocioException(e.getMessage());
		}
		
	}
	
	@CheckSecurity.Restaurantes.PodeGerenciarCadastro
	@DeleteMapping("/{restauranteId}")
	private void remover(@PathVariable Long restauranteId) {
		cadastroRestaurante.remover(restauranteId);
	}
	
	@CheckSecurity.Restaurantes.PodeGerenciarCadastro
    @PutMapping("/{restauranteId}")
    public RestauranteModel atualizar(@PathVariable Long restauranteId,
        @RequestBody @Valid RestauranteInput restauranteInput) {
		try {	
			Restaurante restauranteAtual = cadastroRestaurante.buscarOuFalhar(restauranteId);
		
			restauranteInputDisassembler.copyToDomainObject(restauranteInput, restauranteAtual);
	
			return restauranteModelAssembler.toModel(cadastroRestaurante.salvar(restauranteAtual));
		} catch (CozinhaNaoEncontradaException | CidadeNaoEncontradaException e) {
			throw new NegocioException(e.getMessage());
		}
			
    }
    
	@CheckSecurity.Restaurantes.PodeGerenciarCadastro
    @PutMapping("/{restauranteId}/ativo")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void ativar(@PathVariable Long restauranteId) {
    	cadastroRestaurante.ativar(restauranteId);
    }
    
	@CheckSecurity.Restaurantes.PodeGerenciarCadastro
    @PutMapping("/ativacoes")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void ativarMutiplos(@RequestBody List<Long> restauranteIds) {
    	try {
    		cadastroRestaurante.ativar(restauranteIds);
    	} catch (RestauranteNaoEncontradaException e) {
    		throw new NegocioException(e.getMessage(), e);
    	}	
    }
    
	@CheckSecurity.Restaurantes.PodeGerenciarCadastro
    @DeleteMapping("/{restauranteId}/ativo")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void inativar(@PathVariable Long restauranteId) {
    	cadastroRestaurante.inativar(restauranteId);
    }
    
	@CheckSecurity.Restaurantes.PodeGerenciarCadastro
    @DeleteMapping("/inativacoes")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void inativarMutiplos(@RequestBody List<Long> restauranteIds) {
    	try {
    		cadastroRestaurante.inativar(restauranteIds);
    	} catch (RestauranteNaoEncontradaException e) {
    		throw new NegocioException(e.getMessage(), e);
    	}	
    }
    
	@CheckSecurity.Restaurantes.PodeGerenciarFuncionamento
    @PutMapping("/{restauranteId}/abertura")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void abrir(@PathVariable Long restauranteId) {
        cadastroRestaurante.abrir(restauranteId);
    }
	
	@CheckSecurity.Restaurantes.PodeGerenciarFuncionamento
    @PutMapping("/{restauranteId}/fechamento")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void fechar(@PathVariable Long restauranteId) {
        cadastroRestaurante.fechar(restauranteId);
    } 

}





