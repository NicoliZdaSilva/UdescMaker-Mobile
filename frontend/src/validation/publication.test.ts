import { publicationSchema } from './publication';
import { validPublication } from '../test/fixtures';

describe('publicationSchema', () => {
  it('aceita todos os campos obrigatórios válidos', () => {
    expect(publicationSchema.safeParse(validPublication()).success).toBe(true);
  });

  it('exige os campos obrigatórios, uma categoria e uma tag', () => {
    const values = validPublication();
    values.titulo = '';
    values.categorias = [];
    values.tags = [{ valor: '' }];
    values.capa = null;

    const result = publicationSchema.safeParse(values);
    expect(result.success).toBe(false);
    if (!result.success) {
      const paths = result.error.issues.map((issue) => issue.path[0]);
      expect(paths).toEqual(expect.arrayContaining(['titulo', 'categorias', 'tags', 'capa']));
    }
  });

  it('aplica o limite de 180 caracteres ao resumo', () => {
    const values = validPublication();
    values.resumo = 'a'.repeat(181);
    expect(publicationSchema.safeParse(values).success).toBe(false);
    values.resumo = 'a'.repeat(180);
    expect(publicationSchema.safeParse(values).success).toBe(true);
  });

  it('exige uma descrição do projeto', () => {
    const values = validPublication();
    values.descricaoLonga = '   ';

    const result = publicationSchema.safeParse(values);

    expect(result.success).toBe(false);
    if (!result.success) {
      expect(result.error.issues).toEqual(
        expect.arrayContaining([expect.objectContaining({ path: ['descricaoLonga'] })])
      );
    }
  });

  it.each([
    'https://youtu.be/dQw4w9WgXcQ',
    'https://youtube.com/shorts/dQw4w9WgXcQ',
    'https://www.youtube.com/watch?v=dQw4w9WgXcQ'
  ])('aceita URL segura do YouTube: %s', (url) => {
    const values = validPublication();
    values.videoYoutube = url;
    expect(publicationSchema.safeParse(values).success).toBe(true);
  });

  it('rejeita outro domínio de vídeo', () => {
    const values = validPublication();
    values.videoYoutube = 'https://example.com/watch?v=dQw4w9WgXcQ';
    expect(publicationSchema.safeParse(values).success).toBe(false);
  });

  it.each([
    'http://youtube.com/watch?v=dQw4w9WgXcQ',
    'https://youtube.com/embed/dQw4w9WgXcQ',
    'https://youtube.com/live/dQw4w9WgXcQ',
    'https://music.youtube.com/watch?v=dQw4w9WgXcQ'
  ])('rejeita protocolo ou formato não permitido: %s', (url) => {
    const values = validPublication();
    values.videoYoutube = url;
    expect(publicationSchema.safeParse(values).success).toBe(false);
  });

  it('atribui erros de anexos à lista e ao índice corretos', () => {
    const values = validPublication();
    values.baixaveis = [{ rotulo: 'x', tipo: 'pdf', arquivo: null }];
    values.arquivos = [{ rotulo: 'y', tipo: 'stl', arquivo: null }];

    const result = publicationSchema.safeParse(values);
    expect(result.success).toBe(false);
    if (!result.success) {
      const paths = result.error.issues.map((issue) => issue.path.join('.'));
      expect(paths).toEqual(
        expect.arrayContaining([
          'baixaveis.0.rotulo',
          'baixaveis.0.arquivo',
          'arquivos.0.rotulo',
          'arquivos.0.arquivo'
        ])
      );
    }
  });
});
