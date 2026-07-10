package com.udescmaker.api.repository;

import com.udescmaker.api.domain.*;

import java.util.function.Function;

final class ProjetoUrlResolver {
    private ProjetoUrlResolver() { }

    static Projeto resolver(Projeto p, Function<String, String> resolver) {
        return new Projeto(p.slug(), p.titulo(), p.resumo(), p.publicadoEm(), p.autor(), p.dificuldade(),
                p.idadeMinima(), p.duracaoMinutos(), p.videoYoutube(), p.categorias(), p.tags(), p.destaque(),
                imagem(p.capa(), resolver), p.galeria().stream().map(i -> imagem(i, resolver)).toList(),
                p.materiais(), p.ferramentas(),
                p.passos().stream().map(item -> new PassoProjeto(item.titulo(), item.corpo(),
                        item.imagem() == null ? null : resolver.apply(item.imagem()))).toList(),
                p.dicas(), p.baixaveis().stream().map(a -> arquivo(a, resolver)).toList(),
                p.arquivos().stream().map(a -> arquivo(a, resolver)).toList(), p.relacionados(), p.corpoMarkdown());
    }

    private static ImagemRef imagem(ImagemRef imagem, Function<String, String> resolver) {
        return new ImagemRef(resolver.apply(imagem.src()), imagem.alt());
    }

    private static ArquivoRef arquivo(ArquivoRef arquivo, Function<String, String> resolver) {
        return new ArquivoRef(arquivo.rotulo(), resolver.apply(arquivo.arquivo()), arquivo.tipo());
    }
}
