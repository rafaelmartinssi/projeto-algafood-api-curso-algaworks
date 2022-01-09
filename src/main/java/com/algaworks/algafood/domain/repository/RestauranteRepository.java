package com.algaworks.algafood.domain.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.algaworks.algafood.domain.model.Restaurante;

@Repository
public interface RestauranteRepository extends CustomJpaRepository<Restaurante, Long>, 
RestauranteRepositoryQueries, 
JpaSpecificationExecutor<Restaurante>{
	
	@Query("from Restaurante where nome like %:nome% and cozinha.id = :cozinha")
	List<Restaurante> consultarPorNome(String nome, Long cozinha);
	
	@Query("select case when count(1) > 0 then true else false end from Restaurante rest join rest.responsaveis resp where rest.id = :restauranteId and resp.id = :usuarioId")
	boolean existsResponsavel(Long restauranteId, Long usuarioId);
	
}
