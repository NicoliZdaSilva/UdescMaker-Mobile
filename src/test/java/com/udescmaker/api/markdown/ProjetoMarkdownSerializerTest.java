package com.udescmaker.api.markdown;

import com.udescmaker.api.TestFixtures;
import com.udescmaker.api.taxonomy.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ProjetoMarkdownSerializerTest {
    private final ProjetoMarkdownSerializer serializer = new ProjetoMarkdownSerializer();
    private final FrontmatterParser parser = new FrontmatterParser();

    @Test
    void serializaDeFormaDeterministicaESegura() {
        var projeto = TestFixtures.projeto("aspas", LocalDate.of(2026, 7, 10), Dificuldade.INICIANTE,
                10, 90, List.of(CategoriaProjeto.EDUCACAO), List.of("maker: escola", "aspas \"duplas\""));
        String primeiro = serializer.serializar(projeto);
        String segundo = serializer.serializar(projeto);
        assertThat(primeiro).isEqualTo(segundo).startsWith("---\n").contains("videoYoutube:");
        assertThat(primeiro).contains("\n---\n\nDescrição longa");
    }

    @Test
    void roundTripPreservaContrato() {
        var esperado = TestFixtures.projeto("round-trip", LocalDate.of(2026, 7, 10), Dificuldade.INTERMEDIARIO,
                12, 120, List.of(CategoriaProjeto.ARDUINO, CategoriaProjeto.EDUCACAO), List.of("arduino"));
        var obtido = parser.parse(esperado.slug(), serializer.serializar(esperado));
        assertThat(obtido).isEqualTo(esperado);
    }
}
