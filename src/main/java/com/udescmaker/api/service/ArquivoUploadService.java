package com.udescmaker.api.service;

import com.udescmaker.api.config.UdescMakerProperties;
import com.udescmaker.api.exception.ArquivoInvalidoException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.*;

@Component
public class ArquivoUploadService {
    public static final Set<String> EXTENSOES_IMAGEM = Set.of("png", "jpg", "jpeg", "webp", "avif", "svg");
    private static final Set<String> NOMES_NTFS_RESERVADOS = Set.of(
            "con", "prn", "aux", "nul",
            "com1", "com2", "com3", "com4", "com5", "com6", "com7", "com8", "com9",
            "lpt1", "lpt2", "lpt3", "lpt4", "lpt5", "lpt6", "lpt7", "lpt8", "lpt9");
    private final UdescMakerProperties properties;

    public ArquivoUploadService(UdescMakerProperties properties) { this.properties = properties; }

    public ArquivoValidado ler(MultipartFile arquivo, Set<String> extensoes, String campo) {
        if (arquivo == null || arquivo.isEmpty()) throw new ArquivoInvalidoException(campo + " é obrigatório e não pode estar vazio");
        if (arquivo.getSize() > properties.getUploads().getMaxFileBytes()) {
            throw new ArquivoInvalidoException(campo + " excede o limite de " + properties.getUploads().getMaxFileBytes() + " bytes");
        }
        String original = arquivo.getOriginalFilename();
        if (original == null || original.isBlank()) throw new ArquivoInvalidoException(campo + " não possui nome de arquivo");
        if (original.contains("/") || original.contains("\\") || original.contains("..")) {
            throw new ArquivoInvalidoException(campo + " contém caminho inseguro");
        }
        String extensao = extensao(original);
        if (!extensoes.contains(extensao)) {
            throw new ArquivoInvalidoException(campo + " possui extensão não permitida: ." + extensao);
        }
        try {
            byte[] bytes = arquivo.getBytes();
            validarAssinatura(bytes, extensao, campo);
            return new ArquivoValidado(sanitizarNome(original), extensao, bytes);
        } catch (IOException exception) {
            throw new ArquivoInvalidoException("Não foi possível ler " + campo, exception);
        }
    }

    public String extensao(String nome) {
        int ponto = nome.lastIndexOf('.');
        if (ponto < 1 || ponto == nome.length() - 1) throw new ArquivoInvalidoException("Arquivo sem extensão válida: " + nome);
        return nome.substring(ponto + 1).toLowerCase(Locale.ROOT);
    }

    public String sanitizarNome(String nome) {
        String extensao = extensao(nome);
        String base = nome.substring(0, nome.lastIndexOf('.'));
        base = Normalizer.normalize(base, Normalizer.Form.NFD).replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-").replaceAll("^-+|-+$", "");
        if (base.isBlank()) base = "arquivo";
        if (base.length() > 80) base = base.substring(0, 80).replaceAll("-+$", "");
        if (NOMES_NTFS_RESERVADOS.contains(base)) {
            throw new ArquivoInvalidoException("Nome de arquivo reservado pelo sistema de arquivos: " + base);
        }
        return base + "." + extensao;
    }

    public void validarTotal(long total) {
        if (total > properties.getUploads().getMaxTotalBytes()) {
            throw new ArquivoInvalidoException("Arquivos excedem o limite total de " + properties.getUploads().getMaxTotalBytes() + " bytes");
        }
    }

    private void validarAssinatura(byte[] bytes, String extensao, String campo) {
        if (bytes.length == 0) throw new ArquivoInvalidoException(campo + " está vazio");
        boolean valido = switch (extensao) {
            case "png" -> bytes.length >= 8 && (bytes[0] & 0xff) == 0x89 && bytes[1] == 'P' && bytes[2] == 'N' && bytes[3] == 'G';
            case "jpg", "jpeg" -> bytes.length >= 3 && (bytes[0] & 0xff) == 0xff && (bytes[1] & 0xff) == 0xd8;
            case "pdf" -> inicia(bytes, "%PDF-");
            case "zip", "xlsx", "docx", "sb3" -> bytes.length >= 4 && bytes[0] == 'P' && bytes[1] == 'K';
            case "svg" -> svgSeguro(bytes);
            default -> true;
        };
        if (!valido) throw new ArquivoInvalidoException(campo + " não corresponde ao formato ." + extensao);
    }

    private boolean inicia(byte[] bytes, String prefixo) {
        byte[] esperado = prefixo.getBytes(StandardCharsets.US_ASCII);
        if (bytes.length < esperado.length) return false;
        for (int i = 0; i < esperado.length; i++) if (bytes[i] != esperado[i]) return false;
        return true;
    }

    private boolean svgSeguro(byte[] bytes) {
        if (bytes.length > 2_000_000) return false;
        String texto = new String(bytes, StandardCharsets.UTF_8).toLowerCase(Locale.ROOT);
        return texto.contains("<svg") && !texto.contains("<script") && !texto.contains("javascript:")
                && !texto.matches("(?s).*\\son[a-z]+\\s*=.*");
    }

    public record ArquivoValidado(String nomeSanitizado, String extensao, byte[] bytes) {
        public ArquivoValidado { bytes = bytes.clone(); }
        @Override public byte[] bytes() { return bytes.clone(); }
    }
}
