package com.udescmaker.api.integration.git;

import java.util.List;
import java.time.Instant;

public interface GitRepositoryGateway {
    ResultadoCommit publicar(String slug, List<ArquivoCommit> arquivos, String mensagem);

    record ArquivoCommit(String caminho, byte[] conteudo) {
        public ArquivoCommit {
            conteudo = conteudo.clone();
        }
        @Override public byte[] conteudo() { return conteudo.clone(); }
    }

    record ResultadoCommit(String sha, String url, Instant publicadoEm) { }
}
