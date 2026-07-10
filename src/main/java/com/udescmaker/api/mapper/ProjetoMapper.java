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
                    projeto.dificuldade(),
                    projeto.capa(),
                    projeto.categorias(),
                    projeto.destaque()
            );
       }
    public ProjetoDetalheDTO toDetalhe(Projeto projeto) {
        return new ProjetoDetalheDTO(
                projeto.slug(),
                projeto.titulo(),
                projeto.resumo(),
                projeto.publicadoEm(),
                projeto.autor(),
                projeto.dificuldade(),
                projeto.idadeMinima(),
                projeto.duracaoMinutos(),
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
                projeto.relacionados(),
                projeto.corpoMarkdown()
        );
    }

}
