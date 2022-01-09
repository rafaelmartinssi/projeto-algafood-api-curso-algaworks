package com.algaworks.algafood.domain.service;

import java.io.InputStream;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;

public interface FotoStorageService {
	
	FotoRecuperada recuperar(String novaFoto);
	
	void armazenar(NovaFoto novaFoto);
	
	void remover(String nomeArquivo);
	
	default void substituir(String nomeArquivoAntigo, NovaFoto novaFoto) {
		this.armazenar(novaFoto);

		if(nomeArquivoAntigo != null) {
			this.remover(nomeArquivoAntigo);
		}
	}
	
	default String gerarNomeArquivo(String nomeOriginal) {
		return UUID.randomUUID().toString() + "_" + nomeOriginal;
	}
	
	@Builder
	@Getter
	class NovaFoto {
		private String nomeArquivo;
		private InputStream inputStream;
		private String contentType;
	}
	
	@Builder
	@Getter
	class FotoRecuperada {
		private String url;
		private InputStream inputStream;
		
		public boolean temUrl() {
			return url != null;
		}
		
		public boolean temInputStream() {
			return inputStream != null;
		}
	}
	
}
