package com.udescmaker.api.service;

import com.udescmaker.api.exception.ArquivoInvalidoException;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Set;

@Component
public class SlugService {
    private static final Set<String> NOMES_NTFS_RESERVADOS = Set.of(
            "con", "prn", "aux", "nul",
            "com1", "com2", "com3", "com4", "com5", "com6", "com7", "com8", "com9",
            "lpt1", "lpt2", "lpt3", "lpt4", "lpt5", "lpt6", "lpt7", "lpt8", "lpt9");

    public String gerar(String texto) {
        if (texto == null) throw new ArquivoInvalidoException("Título não pode ser nulo");
        String slug = Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "")
                .replaceAll("-{2,}", "-");
        if (slug.length() > 100) slug = slug.substring(0, 100).replaceAll("-+$", "");
        if (slug.isBlank() || !slug.matches("[a-z0-9]+(?:-[a-z0-9]+)*")) {
            throw new ArquivoInvalidoException("Não foi possível gerar um slug ASCII seguro para o título");
        }
        if (NOMES_NTFS_RESERVADOS.contains(slug)) {
            throw new ArquivoInvalidoException("O título gera um slug reservado pelo sistema de arquivos: " + slug);
        }
        return slug;
    }
}
