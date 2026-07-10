export type DifficultyId = 'iniciante' | 'intermediario' | 'avancado';
export type TipTone = 'info' | 'warning' | 'success';
export type DownloadType = 'pdf' | 'doc' | 'zip';
export type ComplementaryFileType =
  | 'stl'
  | 'jpg'
  | 'png'
  | 'svg'
  | 'zip'
  | 'xlsx'
  | 'other';

export interface TaxonomyOption<T extends string = string> {
  id: T;
  label: string;
}

export interface TaxonomyResponse {
  categorias: TaxonomyOption[];
  dificuldades: TaxonomyOption<DifficultyId>[];
  tonsDica: TaxonomyOption<TipTone>[];
  tiposBaixaveis: TaxonomyOption<DownloadType>[];
  tiposArquivos: TaxonomyOption<ComplementaryFileType>[];
}

export interface Author {
  nome: string;
  github?: string | null;
}

export interface ImageReference {
  src: string;
  alt: string;
}

export interface ProjectStep {
  titulo: string;
  corpo: string;
  imagem?: string | null;
}

export interface ProjectTip {
  tom: TipTone;
  texto: string;
}

export interface ProjectFile {
  rotulo: string;
  arquivo: string;
  tipo: DownloadType | ComplementaryFileType;
}

export interface ProjectSummary {
  slug: string;
  titulo: string;
  resumo: string;
  publicadoEm: string;
  autor: Author;
  dificuldade: DifficultyId;
  idadeMinima: number;
  duracaoMinutos: number;
  categorias: string[];
  tags: string[];
  capa: ImageReference;
  destaque: boolean;
}

export interface ProjectDetail extends ProjectSummary {
  videoYoutube?: string | null;
  galeria: ImageReference[];
  materiais: string[];
  ferramentas: string[];
  passos: ProjectStep[];
  dicas: ProjectTip[];
  baixaveis: ProjectFile[];
  arquivos: ProjectFile[];
  relacionados: ProjectSummary[];
  corpoMarkdown: string;
}

export type ProjectOrder = 'recentes' | 'duracao' | 'dificuldade';

export interface ProjectFilters {
  busca?: string;
  tags?: string[];
  categoria?: string;
  dificuldade?: DifficultyId;
  idade?: number;
  duracaoMaxima?: number;
  ordenacao?: ProjectOrder;
  limite?: number;
}

export interface PublicationResponse {
  slug: string;
  caminho: string;
  shaCommit: string;
  urlCommit?: string | null;
  urlProjeto: string;
  publicadoEm: string;
  mensagem: string;
}

export interface ApiErrorPayload {
  codigo?: string;
  mensagem?: string;
  erro?: string;
  campos?: Record<string, string>;
  timestamp?: string;
  path?: string;
}
