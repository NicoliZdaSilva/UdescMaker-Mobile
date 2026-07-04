package com.udescmaker.api.domain;

import com.udescmaker.api.taxonomy.TipoArquivo;

public record ArquivoRef(String rotulo, String arquivo, TipoArquivo tipo) {
}
