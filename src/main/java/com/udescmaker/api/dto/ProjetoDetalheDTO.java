package com.udescmaker.api.dto;

import com.udescmaker.api.domain.ArquivoRef;
import com.udescmaker.api.domain.Autor;
import com.udescmaker.api.domain.DicaProjeto;
import com.udescmaker.api.domain.ImagemRef;
import com.udescmaker.api.domain.PassoProjeto;
import com.udescmaker.api.taxonomy.CategoriaProjeto;
import com.udescmaker.api.taxonomy.Dificuldade;

import java.time.LocalDate;
import java.util.List;

public record ProjetoDetalheDTO(
        String slug,
        String titulo,
        String resumo,
        LocalDate publicadoEm,
        Autor autor,
        Dificuldade dificuldade,
        int idadeMinima,
        int duracaoMinutos,
        List<CategoriaProjeto> categorias,
        List<String> tags,
        boolean destaque,
        ImagemRef capa,
        List<ImagemRef> galeria,
        List<String> materiais,
        List<String> ferramentas,
        List<PassoProjeto> passos,
        List<DicaProjeto> dicas,
        List<ArquivoRef> baixaveis,
        List<ArquivoRef> arquivos,
        List<String> relacionados,
        String corpoMarkdown
) {
}