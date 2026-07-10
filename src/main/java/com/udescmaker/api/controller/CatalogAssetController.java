package com.udescmaker.api.controller;

import com.udescmaker.api.config.UdescMakerProperties;
import com.udescmaker.api.exception.RecursoNaoEncontradoException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;

@RestController
@ConditionalOnProperty(name = "udescmaker.catalog.mode", havingValue = "local", matchIfMissing = true)
public class CatalogAssetController {
    private final Path raiz;

    public CatalogAssetController(UdescMakerProperties properties) {
        this.raiz = properties.getCatalog().getLocalPath().toAbsolutePath().normalize();
    }

    @GetMapping("/api/catalogo-assets/{slug}/{arquivo:.+}")
    public ResponseEntity<Resource> arquivo(@PathVariable String slug, @PathVariable String arquivo) {
        if (!slug.matches("[a-z0-9]+(?:-[a-z0-9]+)*") || arquivo.contains("/") || arquivo.contains("\\")) {
            throw new RecursoNaoEncontradoException("Arquivo não encontrado");
        }
        Path pastaProjeto = raiz.resolve(slug).normalize();
        Path caminho = pastaProjeto.resolve(arquivo).normalize();
        if (!pastaProjeto.startsWith(raiz) || !caminho.startsWith(pastaProjeto) || !Files.isRegularFile(caminho)) {
            throw new RecursoNaoEncontradoException("Arquivo não encontrado");
        }
        caminho = caminhoRealSeguro(pastaProjeto, caminho);
        MediaType tipo = MediaTypeFactory.getMediaType(arquivo).orElse(MediaType.APPLICATION_OCTET_STREAM);
        return ResponseEntity.ok().contentType(tipo)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + arquivo.replace("\"", "") + "\"")
                .body(new FileSystemResource(caminho));
    }

    private Path caminhoRealSeguro(Path pastaProjeto, Path caminho) {
        try {
            Path raizReal = raiz.toRealPath();
            Path pastaReal = pastaProjeto.toRealPath();
            Path caminhoReal = caminho.toRealPath();
            if (!pastaReal.startsWith(raizReal) || !caminhoReal.startsWith(pastaReal)
                    || !Files.isRegularFile(caminhoReal)) {
                throw new RecursoNaoEncontradoException("Arquivo não encontrado");
            }
            return caminhoReal;
        } catch (IOException exception) {
            throw new RecursoNaoEncontradoException("Arquivo não encontrado");
        }
    }
}
