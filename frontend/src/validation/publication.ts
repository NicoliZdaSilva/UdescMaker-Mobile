import { z } from 'zod';

import type { PublicationFormValues, SelectedFile } from '../types/forms';
import { getYouTubeVideoId } from '../utils/youtube';

const fileSchema = z.custom<SelectedFile | null>((value) => value == null || typeof value === 'object');
const textItem = z.object({ valor: z.string() });

export const publicationSchema = z
  .object({
    titulo: z.string().trim().min(4, 'Informe um título com pelo menos 4 caracteres.'),
    resumo: z
      .string()
      .trim()
      .min(12, 'O resumo deve ter pelo menos 12 caracteres.')
      .max(180, 'O resumo deve ter no máximo 180 caracteres.'),
    autorNome: z.string().trim().min(3, 'Informe o nome da pessoa autora.'),
    autorGithub: z.string(),
    categorias: z.array(z.string()).min(1, 'Selecione pelo menos uma categoria.'),
    tags: z.array(textItem),
    dificuldade: z.enum(['iniciante', 'intermediario', 'avancado'], {
      required_error: 'Selecione a dificuldade.'
    }),
    idadeMinima: z.string().refine((value) => /^\d+$/.test(value) && Number(value) >= 0, {
      message: 'Informe uma idade mínima válida.'
    }),
    duracaoMinutos: z.string().refine((value) => /^\d+$/.test(value) && Number(value) > 0, {
      message: 'Informe uma duração positiva em minutos.'
    }),
    videoYoutube: z.string().trim().refine((value) => getYouTubeVideoId(value) != null, {
      message: 'Informe uma URL válida do YouTube.'
    }),
    capa: fileSchema.refine(Boolean, 'Selecione a imagem de capa.'),
    capaAlt: z.string().trim().min(8, 'Descreva a capa com pelo menos 8 caracteres.'),
    descricaoLonga: z
      .string()
      .trim()
      .min(1, 'Descreva o projeto antes de publicá-lo.')
      .max(30000, 'A descrição deve ter no máximo 30.000 caracteres.'),
    materiais: z.array(textItem),
    ferramentas: z.array(textItem),
    dicas: z.array(z.object({ tom: z.enum(['info', 'warning', 'success']), texto: z.string() })),
    passos: z.array(z.object({ titulo: z.string(), corpo: z.string(), imagem: fileSchema })),
    galeria: z.array(z.object({ alt: z.string(), arquivo: fileSchema })),
    baixaveis: z.array(
      z.object({ rotulo: z.string(), tipo: z.enum(['pdf', 'doc', 'zip']), arquivo: fileSchema })
    ),
    arquivos: z.array(
      z.object({
        rotulo: z.string(),
        tipo: z.enum(['stl', 'jpg', 'png', 'svg', 'zip', 'xlsx', 'other']),
        arquivo: fileSchema
      })
    )
  })
  .superRefine((values, context) => {
    if (!values.tags.some((item) => item.valor.trim().length >= 2)) {
      context.addIssue({
        code: z.ZodIssueCode.custom,
        path: ['tags'],
        message: 'Informe pelo menos uma tag com 2 caracteres.'
      });
    }

    values.galeria.forEach((item, index) => {
      if (item.alt.trim() && !item.arquivo) {
        context.addIssue({
          code: z.ZodIssueCode.custom,
          path: ['galeria', index, 'arquivo'],
          message: 'Selecione o arquivo desta imagem.'
        });
      }
      if (item.arquivo && item.alt.trim().length < 8) {
        context.addIssue({
          code: z.ZodIssueCode.custom,
          path: ['galeria', index, 'alt'],
          message: 'Descreva a imagem com pelo menos 8 caracteres.'
        });
      }
    });

    values.passos.forEach((item, index) => {
      const started = item.titulo.trim() || item.corpo.trim() || item.imagem;
      if (started && item.titulo.trim().length < 4) {
        context.addIssue({
          code: z.ZodIssueCode.custom,
          path: ['passos', index, 'titulo'],
          message: 'O título do passo deve ter pelo menos 4 caracteres.'
        });
      }
      if (started && item.corpo.trim().length < 8) {
        context.addIssue({
          code: z.ZodIssueCode.custom,
          path: ['passos', index, 'corpo'],
          message: 'A descrição do passo deve ter pelo menos 8 caracteres.'
        });
      }
    });

    values.baixaveis.forEach((item, index) => {
      const started = item.rotulo.trim() || item.arquivo;
      if (started && item.rotulo.trim().length < 3) {
        context.addIssue({
          code: z.ZodIssueCode.custom,
          path: ['baixaveis', index, 'rotulo'],
          message: 'Informe um rótulo com pelo menos 3 caracteres.'
        });
      }
      if (item.rotulo.trim() && !item.arquivo) {
        context.addIssue({
          code: z.ZodIssueCode.custom,
          path: ['baixaveis', index, 'arquivo'],
          message: 'Selecione o arquivo baixável.'
        });
      }
    });

    values.arquivos.forEach((item, index) => {
      const started = item.rotulo.trim() || item.arquivo;
      if (started && item.rotulo.trim().length < 3) {
        context.addIssue({
          code: z.ZodIssueCode.custom,
          path: ['arquivos', index, 'rotulo'],
          message: 'Informe um rótulo com pelo menos 3 caracteres.'
        });
      }
      if (item.rotulo.trim() && !item.arquivo) {
        context.addIssue({
          code: z.ZodIssueCode.custom,
          path: ['arquivos', index, 'arquivo'],
          message: 'Selecione o arquivo complementar.'
        });
      }
    });
  });

export const defaultPublicationValues: PublicationFormValues = {
  titulo: '',
  resumo: '',
  autorNome: '',
  autorGithub: '',
  categorias: [],
  tags: [{ valor: '' }],
  dificuldade: '',
  idadeMinima: '',
  duracaoMinutos: '',
  videoYoutube: '',
  capa: null,
  capaAlt: '',
  descricaoLonga: '',
  materiais: [{ valor: '' }],
  ferramentas: [{ valor: '' }],
  dicas: [],
  passos: [],
  galeria: [],
  baixaveis: [],
  arquivos: []
};
