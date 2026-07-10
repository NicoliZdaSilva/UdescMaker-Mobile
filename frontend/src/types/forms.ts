import type {
  ComplementaryFileType,
  DifficultyId,
  DownloadType,
  TipTone
} from './api';

export interface SelectedFile {
  uri: string;
  name: string;
  mimeType: string;
  size?: number;
  webFile?: Blob;
}

export interface TextListItem {
  valor: string;
}

export interface TipFormItem {
  tom: TipTone;
  texto: string;
}

export interface StepFormItem {
  titulo: string;
  corpo: string;
  imagem: SelectedFile | null;
}

export interface GalleryFormItem {
  alt: string;
  arquivo: SelectedFile | null;
}

export interface DownloadFormItem {
  rotulo: string;
  tipo: DownloadType;
  arquivo: SelectedFile | null;
}

export interface ComplementaryFileFormItem {
  rotulo: string;
  tipo: ComplementaryFileType;
  arquivo: SelectedFile | null;
}

export interface PublicationFormValues {
  titulo: string;
  resumo: string;
  autorNome: string;
  autorGithub: string;
  categorias: string[];
  tags: TextListItem[];
  dificuldade: DifficultyId | '';
  idadeMinima: string;
  duracaoMinutos: string;
  videoYoutube: string;
  capa: SelectedFile | null;
  capaAlt: string;
  descricaoLonga: string;
  materiais: TextListItem[];
  ferramentas: TextListItem[];
  dicas: TipFormItem[];
  passos: StepFormItem[];
  galeria: GalleryFormItem[];
  baixaveis: DownloadFormItem[];
  arquivos: ComplementaryFileFormItem[];
}
