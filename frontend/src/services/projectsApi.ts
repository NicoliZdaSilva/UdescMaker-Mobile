import { File as ExpoFile } from 'expo-file-system';
import type {
  ProjectDetail,
  ProjectFilters,
  ProjectSummary,
  PublicationResponse,
  TaxonomyResponse
} from '../types/api';
import type { PublicationFormValues, SelectedFile } from '../types/forms';
import { compactFilters } from '../utils/format';
import { PUBLICATION_REQUEST_TIMEOUT_MS, requestJson } from './apiClient';
import { normalizeProjectDetail, normalizeProjectSummary, normalizeTaxonomy } from './normalizers';

export function buildProjectQuery(filters: ProjectFilters = {}) {
  const query = new URLSearchParams();
  const normalized = compactFilters(filters);

  Object.entries(normalized).forEach(([key, value]) => {
    if (key === 'tags' && Array.isArray(value)) query.set(key, value.join(','));
    else query.set(key, String(value));
  });

  const encoded = query.toString();
  return encoded ? `?${encoded}` : '';
}

export async function listProjects(filters: ProjectFilters = {}): Promise<ProjectSummary[]> {
  const response = await requestJson<unknown[]>(`/projetos${buildProjectQuery(filters)}`);
  return response.map(normalizeProjectSummary);
}

export async function getProject(slug: string): Promise<ProjectDetail> {
  const response = await requestJson<unknown>(`/projetos/${encodeURIComponent(slug)}`);
  return normalizeProjectDetail(response);
}

export async function getTaxonomy(): Promise<TaxonomyResponse> {
  const response = await requestJson<unknown>('/taxonomia');
  return normalizeTaxonomy(response);
}

function cleanTextItems(items: Array<{ valor: string }>) {
  return [...new Set(items.map((item) => item.valor.trim()).filter(Boolean))];
}

function filePart(file: SelectedFile): Blob {
  if (file.webFile) {
    return file.webFile;
  }

  return new ExpoFile(file.uri);
}

export interface PublicationParts {
  projeto: Record<string, unknown>;
  capa: SelectedFile;
  galeria: SelectedFile[];
  passosImagens: SelectedFile[];
  baixaveis: SelectedFile[];
  arquivos: SelectedFile[];
}

export function createPublicationParts(values: PublicationFormValues): PublicationParts {
  if (!values.capa) throw new Error('A imagem de capa é obrigatória.');

  const galleryFiles = values.galeria.filter(
    (item): item is typeof item & { arquivo: SelectedFile } => Boolean(item.arquivo)
  );
  const stepImageFiles: SelectedFile[] = [];
  const downloadFiles = values.baixaveis.filter(
    (item): item is typeof item & { arquivo: SelectedFile } => Boolean(item.arquivo)
  );
  const complementaryFiles = values.arquivos.filter(
    (item): item is typeof item & { arquivo: SelectedFile } => Boolean(item.arquivo)
  );

  const passos = values.passos
    .filter((step) => step.titulo.trim() || step.corpo.trim() || step.imagem)
    .map((step) => {
      const imagemArquivoIndice = step.imagem ? stepImageFiles.push(step.imagem) - 1 : undefined;
      return {
        titulo: step.titulo.trim(),
        corpo: step.corpo.trim(),
        ...(imagemArquivoIndice == null ? {} : { imagemArquivoIndice })
      };
    });

  const github = values.autorGithub.trim();
  const description = values.descricaoLonga.trim();

  return {
    projeto: {
      titulo: values.titulo.trim(),
      resumo: values.resumo.trim(),
      autor: { nome: values.autorNome.trim(), ...(github ? { github } : {}) },
      dificuldade: values.dificuldade,
      idadeMinima: Number(values.idadeMinima),
      duracaoMinutos: Number(values.duracaoMinutos),
      categorias: [...new Set(values.categorias)],
      tags: cleanTextItems(values.tags),
      videoYoutube: values.videoYoutube.trim(),
      capaAlt: values.capaAlt.trim(),
      descricaoLonga: description,
      galeria: galleryFiles.map((item, arquivoIndice) => ({
        alt: item.alt.trim(),
        arquivoIndice
      })),
      materiais: cleanTextItems(values.materiais),
      ferramentas: cleanTextItems(values.ferramentas),
      passos,
      dicas: values.dicas
        .filter((tip) => tip.texto.trim())
        .map((tip) => ({ tom: tip.tom, texto: tip.texto.trim() })),
      baixaveis: downloadFiles.map((item, arquivoIndice) => ({
        rotulo: item.rotulo.trim(),
        tipo: item.tipo,
        arquivoIndice
      })),
      arquivos: complementaryFiles.map((item, arquivoIndice) => ({
        rotulo: item.rotulo.trim(),
        tipo: item.tipo,
        arquivoIndice
      }))
    },
    capa: values.capa,
    galeria: galleryFiles.map((item) => item.arquivo),
    passosImagens: stepImageFiles,
    baixaveis: downloadFiles.map((item) => item.arquivo),
    arquivos: complementaryFiles.map((item) => item.arquivo)
  };
}

export function buildPublicationFormData(values: PublicationFormValues) {
  const parts = createPublicationParts(values);
  const form = new FormData();
  // O FormData nativo do React Native aceita texto ou arquivos { uri, name, type }.
  // Texto JSON mantém o contrato multipart sem depender de Blob, que só funciona no navegador.
  form.append('projeto', JSON.stringify(parts.projeto));
  form.append('capa', filePart(parts.capa));
  parts.galeria.forEach((file) => form.append('galeria', filePart(file)));
  parts.passosImagens.forEach((file) => form.append('passosImagens', filePart(file)));
  parts.baixaveis.forEach((file) => form.append('baixaveis', filePart(file)));
  parts.arquivos.forEach((file) => form.append('arquivos', filePart(file)));
  return form;
}

export async function publishProject(
  values: PublicationFormValues
): Promise<PublicationResponse> {
  const parts = createPublicationParts(values);

  if (__DEV__) {
    console.log('[PUBLICAÇÃO] Arquivos preparados:', {
      capa: {
        uri: parts.capa.uri,
        name: parts.capa.name,
        mimeType: parts.capa.mimeType,
        size: parts.capa.size
      },
      galeria: parts.galeria.map((file) => ({
        uri: file.uri,
        name: file.name,
        mimeType: file.mimeType,
        size: file.size
      })),
      passosImagens: parts.passosImagens.length,
      baixaveis: parts.baixaveis.length,
      arquivos: parts.arquivos.length
    });
  }

  const formData = buildPublicationFormData(values);

  return requestJson<PublicationResponse>('/projetos', {
    method: 'POST',
    body: formData,
    timeoutMs: PUBLICATION_REQUEST_TIMEOUT_MS
  });
}
