package com.udescmaker.api.markdown;

import com.udescmaker.api.exception.MarkdownInvalidoException;
import com.udescmaker.api.taxonomy.TipoArquivo;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class FrontmatterParserTest {
    private final FrontmatterParser parser = new FrontmatterParser();

    @Test
    void aceitaCamposOpcionaisAusentesEAplicaListasVazias() {
        String markdown = """
                ---
                titulo: "Projeto compatível"
                resumo: "Um resumo antigo e compatível"
                publicadoEm: 2026-07-10
                autor:
                  nome: "Pessoa Autora"
                dificuldade: iniciante
                idadeMinima: 8
                duracaoMinutos: 30
                categorias: [educacao]
                tags: [maker]
                capa:
                  src: ./capa.png
                  alt: "Imagem de capa acessível"
                ---

                Corpo preservado.
                """;

        var projeto = parser.parse("compatível", markdown);

        assertThat(projeto.videoYoutube()).isNull();
        assertThat(projeto.galeria()).isEmpty();
        assertThat(projeto.passos()).isEmpty();
        assertThat(projeto.destaque()).isFalse();
        assertThat(projeto.autor().github()).isNull();
        assertThat(projeto.corpoMarkdown()).isEqualTo("Corpo preservado.");
    }

    @Test
    void reconheceXlsxETonsMinusculos() {
        String markdown = base().replace("arquivos: []", """
                dicas:
                  - tom: warning
                    texto: "Cuidado importante"
                arquivos:
                  - rotulo: "Planilha"
                    arquivo: ./lista.xlsx
                    tipo: xlsx""");
        var projeto = parser.parse("teste", markdown);
        assertThat(projeto.arquivos().getFirst().tipo()).isEqualTo(TipoArquivo.XLSX);
        assertThat(projeto.dicas()).hasSize(1);
    }

    @Test
    void rejeitaMarkdownSemDelimitadores() {
        assertThatThrownBy(() -> parser.parse("ruim", "titulo: inválido"))
                .isInstanceOf(MarkdownInvalidoException.class).hasMessageContaining("delimitadores");
    }

    private String base() {
        return """
                ---
                titulo: "Projeto de teste"
                resumo: "Resumo suficientemente longo"
                publicadoEm: 2026-07-10
                autor: {nome: "Pessoa Autora"}
                dificuldade: iniciante
                idadeMinima: 8
                duracaoMinutos: 30
                categorias: [educacao]
                tags: [maker]
                capa: {src: ./capa.png, alt: "Imagem de capa acessível"}
                arquivos: []
                ---
                Corpo.
                """;
    }
}
