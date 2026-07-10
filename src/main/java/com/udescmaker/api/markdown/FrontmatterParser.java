package com.udescmaker.api.markdown;

import com.udescmaker.api.domain.*;
import com.udescmaker.api.exception.MarkdownInvalidoException;
import com.udescmaker.api.taxonomy.CategoriaProjeto;
import com.udescmaker.api.taxonomy.Dificuldade;
import com.udescmaker.api.taxonomy.TipoArquivo;
import com.udescmaker.api.taxonomy.TomDica;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import java.time.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class FrontmatterParser {
    private static final Pattern DELIMITADOR = Pattern.compile("(?m)^---[ \\t]*$");

    private Yaml novoYamlSeguro() {
        LoaderOptions options = new LoaderOptions();
        options.setAllowDuplicateKeys(false);
        options.setMaxAliasesForCollections(30);
        options.setCodePointLimit(2_000_000);
        return new Yaml(new SafeConstructor(options));
    }

    public Projeto parse(String slug, String conteudoArquivo) {
        try {
            if (conteudoArquivo == null) throw erro(slug, "conteúdo ausente");
            String conteudo = conteudoArquivo.replace("\r\n", "\n").replace('\r', '\n');
            if (conteudo.startsWith("\uFEFF")) conteudo = conteudo.substring(1);

            Matcher matcher = DELIMITADOR.matcher(conteudo);
            if (!matcher.find() || matcher.start() != 0 || !matcher.find()) {
                throw erro(slug, "delimitadores '---' de frontmatter ausentes ou inválidos");
            }
            String blocoYaml = conteudo.substring(conteudo.indexOf('\n') + 1, matcher.start());
            String corpo = conteudo.substring(matcher.end()).strip();
            // Yaml não é compartilhado entre requisições: a instância do parser é singleton.
            Object carregado = novoYamlSeguro().load(blocoYaml);
            if (!(carregado instanceof Map<?, ?> mapa)) throw erro(slug, "frontmatter YAML deve ser um objeto");

            String titulo = obrigatorio(mapa, "titulo", slug);
            String resumo = obrigatorio(mapa, "resumo", slug);
            LocalDate publicadoEm = data(mapa.get("publicadoEm"), slug);
            Map<?, ?> autorMap = mapa(mapa.get("autor"), "autor", slug);
            Autor autor = new Autor(obrigatorio(autorMap, "nome", slug), opcional(autorMap.get("github")));
            Dificuldade dificuldade = Dificuldade.fromId(obrigatorio(mapa, "dificuldade", slug));
            int idadeMinima = inteiro(mapa.get("idadeMinima"), "idadeMinima", slug);
            int duracaoMinutos = inteiro(mapa.get("duracaoMinutos"), "duracaoMinutos", slug);
            String videoYoutube = opcional(mapa.get("videoYoutube"));
            List<CategoriaProjeto> categorias = lista(mapa.get("categorias"), "categorias", slug).stream()
                    .map(String::valueOf).map(CategoriaProjeto::fromId).toList();
            List<String> tags = strings(mapa.get("tags"), "tags", slug);
            boolean destaque = booleano(mapa.get("destaque"), false, "destaque", slug);

            Map<?, ?> capaMap = mapa(mapa.get("capa"), "capa", slug);
            ImagemRef capa = new ImagemRef(obrigatorio(capaMap, "src", slug), obrigatorio(capaMap, "alt", slug));
            List<ImagemRef> galeria = mapas(mapa.get("galeria"), "galeria", slug).stream()
                    .map(item -> new ImagemRef(obrigatorio(item, "src", slug), obrigatorio(item, "alt", slug))).toList();
            List<String> materiais = strings(mapa.get("materiais"), "materiais", slug);
            List<String> ferramentas = strings(mapa.get("ferramentas"), "ferramentas", slug);
            List<PassoProjeto> passos = mapas(mapa.get("passos"), "passos", slug).stream()
                    .map(item -> new PassoProjeto(obrigatorio(item, "titulo", slug),
                            obrigatorio(item, "corpo", slug), opcional(item.get("imagem")))).toList();
            List<DicaProjeto> dicas = mapas(mapa.get("dicas"), "dicas", slug).stream()
                    .map(item -> new DicaProjeto(TomDica.fromId(opcionalOu(item.get("tom"), "info")),
                            obrigatorio(item, "texto", slug))).toList();
            List<ArquivoRef> baixaveis = arquivos(mapa.get("baixaveis"), "baixaveis", slug);
            List<ArquivoRef> arquivos = arquivos(mapa.get("arquivos"), "arquivos", slug);
            List<String> relacionados = strings(mapa.get("relacionados"), "relacionados", slug);

            return new Projeto(slug, titulo, resumo, publicadoEm, autor, dificuldade, idadeMinima,
                    duracaoMinutos, videoYoutube, categorias, tags, destaque, capa, galeria, materiais,
                    ferramentas, passos, dicas, baixaveis, arquivos, relacionados, corpo);
        } catch (MarkdownInvalidoException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw erro(slug, exception.getMessage() == null ? "YAML inválido" : exception.getMessage(), exception);
        }
    }

    private List<ArquivoRef> arquivos(Object value, String campo, String slug) {
        return mapas(value, campo, slug).stream().map(item -> new ArquivoRef(
                obrigatorio(item, "rotulo", slug), obrigatorio(item, "arquivo", slug),
                TipoArquivo.fromId(opcionalOu(item.get("tipo"), campo.equals("baixaveis") ? "pdf" : "other")))).toList();
    }

    private String obrigatorio(Map<?, ?> mapa, String campo, String slug) {
        String valor = opcional(mapa.get(campo));
        if (valor == null) throw erro(slug, "campo obrigatório ausente: " + campo);
        return valor;
    }

    private String opcional(Object valor) {
        if (valor == null) return null;
        String texto = String.valueOf(valor).trim();
        return texto.isEmpty() ? null : texto;
    }

    private String opcionalOu(Object valor, String padrao) {
        String texto = opcional(valor);
        return texto == null ? padrao : texto;
    }

    private int inteiro(Object valor, String campo, String slug) {
        if (valor instanceof Number numero) return numero.intValue();
        try { return Integer.parseInt(obrigatorio(Map.of(campo, valor), campo, slug)); }
        catch (RuntimeException exception) { throw erro(slug, "campo inteiro inválido: " + campo, exception); }
    }

    private boolean booleano(Object valor, boolean padrao, String campo, String slug) {
        if (valor == null) return padrao;
        if (valor instanceof Boolean b) return b;
        String texto = String.valueOf(valor);
        if (texto.equalsIgnoreCase("true") || texto.equalsIgnoreCase("false")) return Boolean.parseBoolean(texto);
        throw erro(slug, "campo booleano inválido: " + campo);
    }

    private LocalDate data(Object valor, String slug) {
        if (valor instanceof java.sql.Date date) return date.toLocalDate();
        if (valor instanceof LocalDate localDate) return localDate;
        if (valor instanceof Date date) return date.toInstant().atZone(ZoneOffset.UTC).toLocalDate();
        try { return LocalDate.parse(opcionalOu(valor, "")); }
        catch (DateTimeException exception) { throw erro(slug, "publicadoEm deve usar YYYY-MM-DD", exception); }
    }

    private Map<?, ?> mapa(Object valor, String campo, String slug) {
        if (valor instanceof Map<?, ?> mapa) return mapa;
        throw erro(slug, "campo deve ser um objeto: " + campo);
    }

    private List<?> lista(Object valor, String campo, String slug) {
        if (valor == null) return List.of();
        if (valor instanceof List<?> lista) return lista;
        throw erro(slug, "campo deve ser uma lista: " + campo);
    }

    private List<Map<?, ?>> mapas(Object valor, String campo, String slug) {
        return lista(valor, campo, slug).stream()
                .<Map<?, ?>>map(item -> mapa(item, campo, slug))
                .toList();
    }

    private List<String> strings(Object valor, String campo, String slug) {
        return lista(valor, campo, slug).stream().map(String::valueOf).toList();
    }

    private MarkdownInvalidoException erro(String slug, String detalhe) {
        return new MarkdownInvalidoException("Markdown inválido em '" + slug + "': " + detalhe);
    }

    private MarkdownInvalidoException erro(String slug, String detalhe, Throwable causa) {
        return new MarkdownInvalidoException("Markdown inválido em '" + slug + "': " + detalhe, causa);
    }
}
