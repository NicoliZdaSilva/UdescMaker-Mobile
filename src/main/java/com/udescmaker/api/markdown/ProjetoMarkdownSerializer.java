package com.udescmaker.api.markdown;

import com.udescmaker.api.domain.*;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.representer.Representer;

import java.util.*;

@Component
public class ProjetoMarkdownSerializer {
    private Yaml novoYaml() {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        options.setIndent(2);
        options.setIndicatorIndent(2);
        options.setIndentWithIndicator(true);
        options.setSplitLines(false);
        options.setLineBreak(DumperOptions.LineBreak.UNIX);
        LoaderOptions loaderOptions = new LoaderOptions();
        return new Yaml(new SafeConstructor(loaderOptions), new Representer(options), options, loaderOptions);
    }

    public String serializar(Projeto projeto) {
        Objects.requireNonNull(projeto, "projeto");
        Map<String, Object> dados = new LinkedHashMap<>();
        dados.put("titulo", projeto.titulo());
        dados.put("resumo", projeto.resumo());
        dados.put("publicadoEm", projeto.publicadoEm().toString());
        Map<String, Object> autor = new LinkedHashMap<>();
        autor.put("nome", projeto.autor().nome());
        if (temTexto(projeto.autor().github())) autor.put("github", projeto.autor().github());
        dados.put("autor", autor);
        dados.put("dificuldade", projeto.dificuldade().getId());
        dados.put("idadeMinima", projeto.idadeMinima());
        dados.put("duracaoMinutos", projeto.duracaoMinutos());
        dados.put("categorias", new ArrayList<>(projeto.categorias().stream().map(c -> c.getId()).toList()));
        dados.put("tags", new ArrayList<>(projeto.tags()));
        dados.put("destaque", projeto.destaque());
        if (temTexto(projeto.videoYoutube())) dados.put("videoYoutube", projeto.videoYoutube());
        dados.put("capa", imagem(projeto.capa()));
        dados.put("galeria", new ArrayList<>(projeto.galeria().stream().map(this::imagem).toList()));
        dados.put("materiais", new ArrayList<>(projeto.materiais()));
        dados.put("ferramentas", new ArrayList<>(projeto.ferramentas()));
        dados.put("passos", new ArrayList<>(projeto.passos().stream().map(this::passo).toList()));
        dados.put("dicas", new ArrayList<>(projeto.dicas().stream().map(this::dica).toList()));
        dados.put("baixaveis", new ArrayList<>(projeto.baixaveis().stream().map(this::arquivo).toList()));
        dados.put("arquivos", new ArrayList<>(projeto.arquivos().stream().map(this::arquivo).toList()));
        dados.put("relacionados", new ArrayList<>(projeto.relacionados()));

        String blocoYaml = novoYaml().dump(dados);
        String corpo = projeto.corpoMarkdown() == null ? "" : projeto.corpoMarkdown().strip();
        return "---\n" + blocoYaml + "---\n" + (corpo.isEmpty() ? "" : "\n" + corpo + "\n");
    }

    private Map<String, Object> imagem(ImagemRef imagem) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("src", imagem.src());
        item.put("alt", imagem.alt());
        return item;
    }

    private Map<String, Object> passo(PassoProjeto passo) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("titulo", passo.titulo());
        item.put("corpo", passo.corpo());
        if (temTexto(passo.imagem())) item.put("imagem", passo.imagem());
        return item;
    }

    private Map<String, Object> dica(DicaProjeto dica) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("tom", dica.tom().getId());
        item.put("texto", dica.texto());
        return item;
    }

    private Map<String, Object> arquivo(ArquivoRef arquivo) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("rotulo", arquivo.rotulo());
        item.put("arquivo", arquivo.arquivo());
        item.put("tipo", arquivo.tipo().getId());
        return item;
    }

    private boolean temTexto(String texto) { return texto != null && !texto.isBlank(); }
}
