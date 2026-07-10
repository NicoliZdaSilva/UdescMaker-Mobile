import type { ProjectSummary } from '../types/api';
import type { PublicationFormValues, SelectedFile } from '../types/forms';

export const coverFile: SelectedFile = {
  uri: 'file:///tmp/capa.jpg',
  name: 'capa.jpg',
  mimeType: 'image/jpeg',
  size: 1024
};

export function validPublication(): PublicationFormValues {
  return {
    titulo: 'Projeto de teste',
    resumo: 'Resumo válido com mais de doze caracteres.',
    autorNome: 'Maria Maker',
    autorGithub: 'mariamaker',
    categorias: ['educacao'],
    tags: [{ valor: 'maker' }],
    dificuldade: 'iniciante',
    idadeMinima: '10',
    duracaoMinutos: '90',
    videoYoutube: 'https://www.youtube.com/watch?v=dQw4w9WgXcQ',
    capa: coverFile,
    capaAlt: 'Capa acessível do projeto',
    descricaoLonga: 'Descrição longa.',
    materiais: [{ valor: 'Papelão' }],
    ferramentas: [{ valor: 'Tesoura' }],
    dicas: [{ tom: 'warning', texto: 'Use a tesoura com cuidado.' }],
    passos: [{ titulo: 'Preparar peças', corpo: 'Recorte todas as peças.', imagem: null }],
    galeria: [],
    baixaveis: [],
    arquivos: []
  };
}

export function projectSummary(overrides: Partial<ProjectSummary> = {}): ProjectSummary {
  return {
    slug: 'projeto-teste',
    titulo: 'Projeto teste',
    resumo: 'Um resumo do projeto para o cartão.',
    publicadoEm: '2026-07-10',
    autor: { nome: 'Maria Maker' },
    dificuldade: 'iniciante',
    idadeMinima: 10,
    duracaoMinutos: 90,
    categorias: ['educacao'],
    tags: ['maker'],
    capa: { src: 'https://example.com/capa.svg', alt: 'Capa do projeto teste' },
    destaque: false,
    ...overrides
  };
}
