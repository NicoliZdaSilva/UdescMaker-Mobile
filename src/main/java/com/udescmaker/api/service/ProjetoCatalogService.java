package com.udescmaker.api.service;

import com.udescmaker.api.domain.Projeto;
import com.udescmaker.api.markdown.FrontmatterParser;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProjetoCatalogService {

    private static final String TEXTO_PROJETO_1 = """
            ---
            titulo: "Giz de Lousa Feito com Terra"
            resumo: "Um giz caseiro sustentável"
            dificuldade: "iniciante"
            idadeMinima: 10
            duracaoMinutos: 30
            destaque: true
            publicadoEm: 2026-06-03
            autor:
              nome: "Lucas"
              github: "lucas-dev"
            tags: ["sustentabilidade", "iniciante"]
            categorias: ["educacao", "sustentabilidade"]
            materiais: ["Terra", "Água", "Recipiente"]
            ferramentas: ["Colher", "Peneira"]
            capa:
              src: "./capa.svg"
              alt: "Capa do projeto"
            galeria:
              - src: "./foto1.jpg"
                alt: "Foto 1"
            passos:
              - titulo: "Passo 1"
                corpo: "Misture a terra com água"
            dicas:
              - tom: "INFO"
                texto: "Use luvas"
            baixaveis:
              - rotulo: "Manual em PDF"
                arquivo: "./manual.pdf"
                tipo: "pdf"
            arquivos:
              - rotulo: "Modelo 3D"
                arquivo: "./modelo.stl"
                tipo: "stl"
            relacionados: []
            ---

            Corpo do projeto em markdown aqui.
            """;

    private final List<Projeto> projetos;

    public ProjetoCatalogService(FrontmatterParser parser) {
        Projeto p1 = parser.parse("giz-de-lousa", TEXTO_PROJETO_1);

        this.projetos = List.of(p1);
    }

    public List<Projeto> listarTodos() {
        return projetos;
    }

    public Optional<Projeto> buscarPorSlug(String slug) {
        return projetos.stream()
                .filter(p -> p.slug().equals(slug))
                .findFirst();
    }
}

