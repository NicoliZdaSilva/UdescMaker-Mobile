package com.udescmaker.api.mapper;

import com.udescmaker.api.domain.Projeto;
import com.udescmaker.api.dto.ProjetoDetalheDTO;
import com.udescmaker.api.dto.ProjetoResumoDTO;
import org.springframework.stereotype.Component;

@Component
public class ProjetoMapper {


        public ProjetoResumoDTO toResumo(Projeto projeto){
            return new ProjetoResumoDTO(
                    projeto.slug(),
                    projeto.titulo(),
                    projeto.resumo(),
                    projeto.publicadoEm(),
                    projeto.autor(),
                    projeto.dificuldade(),
                    projeto.idadeMinima(),
                    projeto.duracaoMinutos(),
                    projeto.capa(),
                    projeto.categorias(),
                    projeto.tags(),
                    projeto.destaque()
            );
       }
    public ProjetoDetalheDTO toDetalhe(Projeto projeto) {
        return toDetalhe(projeto, java.util.List.of());
    }

    public ProjetoDetalheDTO toDetalhe(Projeto projeto, java.util.List<Projeto> relacionados) {
        return new ProjetoDetalheDTO(
                projeto.slug(),
                projeto.titulo(),
                projeto.resumo(),
                projeto.publicadoEm(),
                projeto.autor(),
                projeto.dificuldade(),
                projeto.idadeMinima(),
                projeto.duracaoMinutos(),
                projeto.videoYoutube(),
                projeto.categorias(),
                projeto.tags(),
                projeto.destaque(),
                projeto.capa(),
                projeto.galeria(),
                projeto.materiais(),
                projeto.ferramentas(),
                projeto.passos(),
                projeto.dicas(),
                projeto.baixaveis(),
                projeto.arquivos(),
                relacionados.stream().map(this::toResumo).toList(),
                projeto.corpoMarkdown()
        );
    }

}
