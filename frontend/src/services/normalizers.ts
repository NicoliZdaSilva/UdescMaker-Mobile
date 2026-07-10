import type {
  ComplementaryFileType,
  DifficultyId,
  DownloadType,
  ProjectDetail,
  ProjectFile,
  ProjectSummary,
  ProjectTip,
  TaxonomyOption,
  TaxonomyResponse,
  TipTone
} from '../types/api';

function asObject(value: unknown): Record<string, unknown> {
  return value && typeof value === 'object' ? (value as Record<string, unknown>) : {};
}

function asString(value: unknown, fallback = '') {
  return typeof value === 'string' ? value : fallback;
}

function asNumber(value: unknown, fallback = 0) {
  const result = Number(value);
  return Number.isFinite(result) ? result : fallback;
}

function asArray(value: unknown) {
  return Array.isArray(value) ? value : [];
}

function normalizeId(value: unknown) {
  return asString(value).trim().toLowerCase().replaceAll('_', '-');
}

function normalizeOption<T extends string>(value: unknown): TaxonomyOption<T> {
  const option = asObject(value);
  return {
    id: normalizeId(option.id) as T,
    label: asString(option.label, asString(option.id))
  };
}

function normalizeAuthor(value: unknown) {
  const author = asObject(value);
  return {
    nome: asString(author.nome, 'Autoria não informada'),
    github: asString(author.github) || null
  };
}

function normalizeImage(value: unknown) {
  const image = asObject(value);
  return { src: asString(image.src), alt: asString(image.alt, 'Imagem do projeto') };
}

function normalizeFile(value: unknown): ProjectFile {
  const file = asObject(value);
  return {
    rotulo: asString(file.rotulo, 'Arquivo'),
    arquivo: asString(file.arquivo),
    tipo: normalizeId(file.tipo) as DownloadType | ComplementaryFileType
  };
}

export function normalizeProjectSummary(value: unknown): ProjectSummary {
  const project = asObject(value);
  return {
    slug: asString(project.slug),
    titulo: asString(project.titulo),
    resumo: asString(project.resumo),
    publicadoEm: asString(project.publicadoEm),
    autor: normalizeAuthor(project.autor),
    dificuldade: normalizeId(project.dificuldade) as DifficultyId,
    idadeMinima: asNumber(project.idadeMinima),
    duracaoMinutos: asNumber(project.duracaoMinutos),
    categorias: asArray(project.categorias).map(normalizeId).filter(Boolean),
    tags: asArray(project.tags).map((item) => asString(item)).filter(Boolean),
    capa: normalizeImage(project.capa),
    destaque: Boolean(project.destaque)
  };
}

export function normalizeProjectDetail(value: unknown): ProjectDetail {
  const project = asObject(value);
  const summary = normalizeProjectSummary(project);
  return {
    ...summary,
    videoYoutube: asString(project.videoYoutube) || null,
    galeria: asArray(project.galeria).map(normalizeImage),
    materiais: asArray(project.materiais).map((item) => asString(item)).filter(Boolean),
    ferramentas: asArray(project.ferramentas).map((item) => asString(item)).filter(Boolean),
    passos: asArray(project.passos).map((item) => {
      const step = asObject(item);
      return {
        titulo: asString(step.titulo),
        corpo: asString(step.corpo),
        imagem: asString(step.imagem) || null
      };
    }),
    dicas: asArray(project.dicas).map((item) => {
      const tip = asObject(item);
      return {
        tom: normalizeId(tip.tom) as TipTone,
        texto: asString(tip.texto)
      } satisfies ProjectTip;
    }),
    baixaveis: asArray(project.baixaveis).map(normalizeFile),
    arquivos: asArray(project.arquivos).map(normalizeFile),
    relacionados: asArray(project.relacionados).map(normalizeProjectSummary),
    corpoMarkdown: asString(project.corpoMarkdown, asString(project.descricaoLonga))
  };
}

export function normalizeTaxonomy(value: unknown): TaxonomyResponse {
  const taxonomy = asObject(value);
  return {
    categorias: asArray(taxonomy.categorias).map(normalizeOption),
    dificuldades: asArray(taxonomy.dificuldades).map(normalizeOption<DifficultyId>),
    tonsDica: asArray(taxonomy.tonsDica).map(normalizeOption<TipTone>),
    tiposBaixaveis: asArray(taxonomy.tiposBaixaveis).map(normalizeOption<DownloadType>),
    tiposArquivos: asArray(taxonomy.tiposArquivos).map(normalizeOption<ComplementaryFileType>)
  };
}
