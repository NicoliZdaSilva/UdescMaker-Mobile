package com.udescmaker.api.dto;

import com.udescmaker.api.taxonomy.CategoriaProjeto;
import com.udescmaker.api.taxonomy.Dificuldade;
import com.udescmaker.api.taxonomy.TipoArquivo;
import com.udescmaker.api.taxonomy.TomDica;
import com.udescmaker.api.validation.YoutubeUrl;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;

public record ProjetoPublicacaoRequest(
        @NotBlank @Size(min = 4, max = 120) String titulo,
        @NotBlank @Size(min = 12, max = 180) String resumo,
        @NotNull @Valid AutorPublicacao autor,
        @NotNull Dificuldade dificuldade,
        @NotNull @PositiveOrZero Integer idadeMinima,
        @NotNull @Positive Integer duracaoMinutos,
        @NotEmpty @Size(max = 13) List<@NotNull CategoriaProjeto> categorias,
        @NotEmpty @Size(max = 20) List<@NotBlank @Size(min = 2, max = 40) String> tags,
        @NotBlank @YoutubeUrl String videoYoutube,
        @NotBlank @Size(min = 8, max = 240) String capaAlt,
        @NotBlank @Size(max = 30000) String descricaoLonga,
        @Size(max = 20) List<@NotNull @Valid GaleriaPublicacao> galeria,
        @Size(max = 100) List<@NotBlank @Size(min = 2, max = 200) String> materiais,
        @Size(max = 100) List<@NotBlank @Size(min = 2, max = 200) String> ferramentas,
        @Size(max = 50) List<@NotNull @Valid PassoPublicacao> passos,
        @Size(max = 50) List<@NotNull @Valid DicaPublicacao> dicas,
        @Size(max = 20) List<@NotNull @Valid ArquivoPublicacao> baixaveis,
        @Size(max = 20) List<@NotNull @Valid ArquivoPublicacao> arquivos
) {
    public record AutorPublicacao(
            @NotBlank @Size(min = 3, max = 120) String nome,
            @Pattern(regexp = "^$|^[A-Za-z0-9](?:[A-Za-z0-9-]{0,37}[A-Za-z0-9])?$", message = "usuário do GitHub inválido") String github) {}

    public record GaleriaPublicacao(
            @NotBlank @Size(min = 8, max = 240) String alt,
            @NotNull @PositiveOrZero Integer arquivoIndice) {}

    public record PassoPublicacao(
            @NotBlank @Size(min = 4, max = 160) String titulo,
            @NotBlank @Size(min = 8, max = 5000) String corpo,
            @PositiveOrZero Integer imagemArquivoIndice) {}

    public record DicaPublicacao(
            @NotNull TomDica tom,
            @NotBlank @Size(min = 4, max = 1000) String texto) {}

    public record ArquivoPublicacao(
            @NotBlank @Size(min = 3, max = 160) String rotulo,
            @NotNull TipoArquivo tipo,
            @NotNull @PositiveOrZero Integer arquivoIndice) {}
}
