package com.udescmaker.api.repository;

import com.udescmaker.api.domain.Projeto;
import com.udescmaker.api.markdown.FrontmatterParser;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Repository
public class MockProjetoRepository implements ProjetoRepository {

    private final List<Projeto> projetos;

    public MockProjetoRepository(FrontmatterParser parser) throws IOException {

        PathMatchingResourcePatternResolver resolver =
                new PathMatchingResourcePatternResolver();

        Resource[] arquivos =
                resolver.getResources("classpath:mock/*.md");

        List<Projeto> lista = new ArrayList<>();

        for (Resource arquivo : arquivos) {

            String nomeArquivo = arquivo.getFilename();

            String slug = nomeArquivo.substring(0, nomeArquivo.lastIndexOf("."));

            String conteudo = new String(
                    arquivo.getInputStream().readAllBytes(),
                    StandardCharsets.UTF_8
            );

            lista.add(parser.parse(slug, conteudo));
        }

        this.projetos = List.copyOf(lista);
    }

    @Override
    public List<Projeto> listarTodos() {
        return projetos;
    }
}