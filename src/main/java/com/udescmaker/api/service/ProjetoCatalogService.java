package com.udescmaker.api.service;

import com.udescmaker.api.domain.Projeto;
import com.udescmaker.api.repository.ProjetoRepository;
import com.udescmaker.api.taxonomy.*;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

@Service
public class ProjetoCatalogService {
    private final ProjetoRepository repository;

    public ProjetoCatalogService(ProjetoRepository repository) { this.repository = repository; }

    public List<Projeto> listarTodos() { return repository.listarTodos(); }
    public Optional<Projeto> buscarPorSlug(String slug) { return repository.buscarPorSlug(slug); }

    public List<Projeto> filtrar(Filtro filtro) {
        Objects.requireNonNull(filtro, "filtro");
        String busca = normalizar(filtro.busca());
        Set<String> tags = filtro.tags() == null ? Set.of() : filtro.tags().stream()
                .flatMap(tag -> Arrays.stream(tag.split(","))).map(this::normalizar).filter(s -> !s.isBlank())
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
        Comparator<Projeto> comparador = comparador(filtro.ordenacao());
        Stream<Projeto> projetos = repository.listarTodos().stream()
                .filter(p -> filtro.categoria() == null || p.categorias().contains(filtro.categoria()))
                .filter(p -> filtro.dificuldade() == null || p.dificuldade() == filtro.dificuldade())
                .filter(p -> filtro.idade() == null || p.idadeMinima() <= filtro.idade())
                .filter(p -> filtro.duracaoMaxima() == null || p.duracaoMinutos() <= filtro.duracaoMaxima())
                .filter(p -> tags.isEmpty() || tags.stream().allMatch(tag -> p.tags().stream()
                        .map(this::normalizar).anyMatch(valor -> valor.contains(tag))))
                .filter(p -> busca.isBlank() || textoBusca(p).contains(busca))
                .sorted(comparador);
        if (filtro.limite() != null) projetos = projetos.limit(filtro.limite());
        return projetos.toList();
    }

    /** Compatibilidade com o contrato anterior. */
    public List<Projeto> filtrar(String busca, CategoriaProjeto categoria, Dificuldade dificuldade,
                                 Integer idade, Integer duracaoMaxima) {
        return filtrar(new Filtro(busca, List.of(), categoria, dificuldade, idade, duracaoMaxima,
                OrdenacaoProjeto.RECENTES, null));
    }

    public List<Projeto> listarDestaques() {
        return repository.listarTodos().stream().filter(Projeto::destaque)
                .sorted(comparador(OrdenacaoProjeto.RECENTES)).toList();
    }

    public List<Projeto> relacionados(Projeto atual) {
        return repository.listarTodos().stream()
                .filter(candidato -> !candidato.slug().equals(atual.slug()))
                .map(candidato -> new Pontuado(candidato, pontuar(atual, candidato)))
                .filter(item -> item.pontos > 0)
                .sorted(Comparator.comparingInt(Pontuado::pontos).reversed()
                        .thenComparing(item -> item.projeto.publicadoEm(), Comparator.reverseOrder())
                        .thenComparing(item -> item.projeto.slug()))
                .limit(3).map(Pontuado::projeto).toList();
    }

    int pontuar(Projeto atual, Projeto candidato) {
        long categorias = candidato.categorias().stream().distinct().filter(atual.categorias()::contains).count();
        Set<String> tagsAtual = atual.tags().stream().map(this::normalizar).collect(java.util.stream.Collectors.toSet());
        long tags = candidato.tags().stream().map(this::normalizar).distinct().filter(tagsAtual::contains).count();
        return Math.toIntExact(categorias * 5 + tags * 3);
    }

    public void invalidarCache() { repository.invalidarCache(); }

    private Comparator<Projeto> comparador(OrdenacaoProjeto ordenacao) {
        OrdenacaoProjeto efetiva = ordenacao == null ? OrdenacaoProjeto.RECENTES : ordenacao;
        Comparator<Projeto> recentes = Comparator.comparing(Projeto::publicadoEm).reversed()
                .thenComparing(Projeto::slug);
        return switch (efetiva) {
            case RECENTES -> recentes;
            case DURACAO -> Comparator.comparingInt(Projeto::duracaoMinutos).thenComparing(recentes);
            case DIFICULDADE -> Comparator.comparingInt((Projeto p) -> p.dificuldade().ordinal()).thenComparing(recentes);
        };
    }

    private String textoBusca(Projeto p) {
        return Stream.of(Stream.of(p.titulo(), p.resumo(), p.corpoMarkdown()), p.tags().stream(),
                        p.materiais().stream(), p.ferramentas().stream(),
                        p.passos().stream().flatMap(item -> Stream.of(item.titulo(), item.corpo())),
                        p.dicas().stream().map(item -> item.texto()))
                .flatMap(Function.identity()).filter(Objects::nonNull).map(this::normalizar)
                .reduce("", (a, b) -> a + " " + b);
    }

    private String normalizar(String valor) {
        if (valor == null) return "";
        return Normalizer.normalize(valor, Normalizer.Form.NFD).replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT).trim();
    }

    public record Filtro(String busca, List<String> tags, CategoriaProjeto categoria,
                         Dificuldade dificuldade, Integer idade, Integer duracaoMaxima,
                         OrdenacaoProjeto ordenacao, Integer limite) {
        public Filtro {
            if (idade != null && idade < 0) throw new IllegalArgumentException("idade deve ser maior ou igual a zero");
            if (duracaoMaxima != null && duracaoMaxima <= 0) throw new IllegalArgumentException("duracaoMaxima deve ser positiva");
            if (limite != null && (limite < 1 || limite > 100)) throw new IllegalArgumentException("limite deve estar entre 1 e 100");
        }
    }
    private record Pontuado(Projeto projeto, int pontos) { }
}
