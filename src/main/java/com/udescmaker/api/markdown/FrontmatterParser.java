package com.udescmaker.api.markdown;
import com.udescmaker.api.domain.*;
import com.udescmaker.api.taxonomy.CategoriaProjeto;
import com.udescmaker.api.taxonomy.Dificuldade;
import com.udescmaker.api.taxonomy.TipoArquivo;
import com.udescmaker.api.taxonomy.TomDica;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Component
public class FrontmatterParser {
    public Projeto parse(String slug, String conteudoArquivo) {
        String[] partes = conteudoArquivo.split("---", 3);
        String blocoYaml = partes[1];
        String corpo = partes[2].trim();

        Yaml yaml = new Yaml();
        Map<String, Object> dados = yaml.load(blocoYaml);

        //Recuperando dados da YAML
        String titulo = (String) dados.get("titulo");
        String resumo = (String) dados.get("resumo");
        String dificuldadeTexto = (String) dados.get("dificuldade");
        Dificuldade dificuldade = Dificuldade.fromId(dificuldadeTexto);
        int idadeMinima = (Integer) dados.get("idadeMinima");
        int duracaoMinutos = (Integer) dados.get("duracaoMinutos");
        boolean destaque = (Boolean) dados.get("destaque");
        Date dataAntiga = (Date) dados.get("publicadoEm");
        LocalDate publicadoEm = dataAntiga.toInstant().atZone(ZoneOffset.UTC).toLocalDate();
        Map<String, Object> autorMap = (Map<String, Object>) dados.get("autor");
        Autor autor = new Autor((String) autorMap.get("nome"), (String) autorMap.get("github"));
        List<String> tags = ((List<?>) dados.get("tags")).stream()
                .map(String::valueOf)
                .toList();
        List<CategoriaProjeto> categorias = ((List<?>) dados.get("categorias")).stream()
                .map(String::valueOf)
                .map(CategoriaProjeto::fromId)
                .toList();
        List<String> materiais = ((List<?>) dados.get("materiais")).stream()
                .map(String::valueOf)
                .toList();
        List<String> ferramentas = ((List<?>) dados.get("ferramentas")).stream()
                .map(String::valueOf)
                .toList();
        Map<String, Object> capaMap = (Map<String, Object>) dados.get("capa");
        ImagemRef capa = new ImagemRef((String) capaMap.get("src"), (String) capaMap.get("alt"));
        List<ImagemRef> galeria = ((List<Map<String, Object>>) dados.get("galeria")).stream()
                .map(item -> new ImagemRef((String) item.get("src"), (String) item.get("alt")))
                .toList();
        List<PassoProjeto> passos = ((List<Map<String, Object>>)dados.get("passos")).stream()
                .map(item -> new PassoProjeto((String) item.get("titulo"), (String) item.get("corpo"), (String) item.get("imagem")))
                .toList();
        List<DicaProjeto> dicas = ((List<Map<String, Object>>)dados.get("dicas")).stream()
                .map(item -> new DicaProjeto(TomDica.valueOf(((String) item.get("tom")).toUpperCase()), (String) item.get("texto")))
                .toList();
        List<ArquivoRef> baixaveis = ((List<Map<String, Object>>) dados.get("baixaveis")).stream()
                .map(item -> new ArquivoRef(
                        (String) item.get("rotulo"),
                        (String) item.get("arquivo"),
                        TipoArquivo.valueOf(((String) item.get("tipo")).toUpperCase())))
                .toList();

        List<ArquivoRef> arquivos = ((List<Map<String, Object>>) dados.get("arquivos")).stream()
                .map(item -> new ArquivoRef(
                        (String) item.get("rotulo"),
                        (String) item.get("arquivo"),
                        TipoArquivo.valueOf(((String) item.get("tipo")).toUpperCase())))
                .toList();
        List<String> relacionados = ((List<?>) dados.get("relacionados")).stream()
                .map(String::valueOf)
                .toList();

        return new Projeto(
                slug,
                titulo,
                resumo,
                publicadoEm,
                autor,
                dificuldade,
                idadeMinima,
                duracaoMinutos,
                categorias,
                tags,
                destaque,
                capa,
                galeria,
                materiais,
                ferramentas,
                passos,
                dicas,
                baixaveis,
                arquivos,
                relacionados,
                corpo
        );
    }
}
