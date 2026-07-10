package com.udescmaker.api.repository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import org.springframework.web.util.UriUtils;

import com.udescmaker.api.config.UdescMakerProperties;
import com.udescmaker.api.domain.Projeto;
import com.udescmaker.api.exception.ConfiguracaoException;
import com.udescmaker.api.markdown.FrontmatterParser;

@Repository
@ConditionalOnProperty(name = "udescmaker.catalog.mode", havingValue = "local", matchIfMissing = true)
public class LocalProjetoRepository implements ProjetoRepository {
    private final FrontmatterParser parser;
    private final UdescMakerProperties properties;
    private final Clock clock;
    private volatile Cache cache;

    @Autowired
    public LocalProjetoRepository(FrontmatterParser parser, UdescMakerProperties properties) {
        this(parser, properties, Clock.systemUTC());
    }

    LocalProjetoRepository(FrontmatterParser parser, UdescMakerProperties properties, Clock clock) {
        this.parser = parser;
        this.properties = properties;
        this.clock = clock;
    }

    @Override
    public List<Projeto> listarTodos() {
        Cache atual = cache;
        if (atual != null && clock.instant().isBefore(atual.carregadoEm.plus(properties.getCatalog().getCacheTtl()))) {
            return atual.projetos;
        }
        synchronized (this) {
            atual = cache;
            if (atual == null || !clock.instant().isBefore(atual.carregadoEm.plus(properties.getCatalog().getCacheTtl()))) {
                cache = new Cache(clock.instant(), carregar());
            }
            return cache.projetos;
        }
    }

    private List<Projeto> carregar() {
        Path raiz = properties.getCatalog().getLocalPath().toAbsolutePath().normalize();
        if (!Files.isDirectory(raiz)) {
            throw new ConfiguracaoException("Diretório local do catálogo não encontrado: " + raiz);
        }
        try (var pastas = Files.list(raiz)) {
            return pastas.filter(Files::isDirectory)
                    .map(pasta -> pasta.resolve("index.md"))
                    .filter(Files::isRegularFile)
                    .sorted(Comparator.comparing(Path::toString))
                    .map(this::parse)
                    .toList();
        } catch (IOException exception) {
            throw new ConfiguracaoException("Não foi possível ler o catálogo local", exception);
        }
    }

    private Projeto parse(Path index) {
        try {
            String slug = index.getParent().getFileName().toString();
            Projeto projeto = parser.parse(slug, Files.readString(index, StandardCharsets.UTF_8));
            String base = properties.getApiPublicUrl().toString().replaceAll("/$", "")
                    + "/api/catalogo-assets/" + segmento(slug) + "/";
            return ProjetoUrlResolver.resolver(projeto, caminho -> resolver(base, caminho));
        } catch (IOException exception) {
            throw new ConfiguracaoException("Não foi possível ler " + index, exception);
        }
    }

    private String resolver(String base, String caminho) {
        if (caminho == null || caminho.matches("(?i)^https?://.*")) return caminho;
        String nome = caminho.replaceFirst("^\\./", "");
        if (nome.contains("/") || nome.contains("\\") || nome.equals("..")) {
            throw new ConfiguracaoException("Referência de arquivo insegura no catálogo: " + caminho);
        }
        return base + segmento(nome);
    }

    private String segmento(String valor) { return UriUtils.encodePathSegment(valor, StandardCharsets.UTF_8); }

    @Override public void invalidarCache() { cache = null; }
    private record Cache(Instant carregadoEm, List<Projeto> projetos) { }
}
