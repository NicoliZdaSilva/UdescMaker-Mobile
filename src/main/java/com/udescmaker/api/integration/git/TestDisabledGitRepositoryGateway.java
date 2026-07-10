package com.udescmaker.api.integration.git;

import com.udescmaker.api.exception.ConfiguracaoException;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Profile("test")
public class TestDisabledGitRepositoryGateway implements GitRepositoryGateway {
    @Override
    public ResultadoCommit publicar(String slug, List<ArquivoCommit> arquivos, String mensagem) {
        throw new ConfiguracaoException("Publicação real é proibida no perfil test");
    }
}
