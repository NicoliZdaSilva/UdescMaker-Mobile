import { buildProjectQuery, buildPublicationFormData, createPublicationParts } from './projectsApi';
import { coverFile, validPublication } from '../test/fixtures';
import { PUBLICATION_REQUEST_TIMEOUT_MS } from './apiClient';

describe('projectsApi', () => {
  it('reserva cinco minutos para o upload atômico de metadados e anexos', () => {
    expect(PUBLICATION_REQUEST_TIMEOUT_MS).toBe(300_000);
  });

  it('monta filtros combinados e ordenação no contrato do backend', () => {
    const query = buildProjectQuery({
      busca: 'horta',
      tags: ['pet', 'água'],
      categoria: 'sustentabilidade',
      dificuldade: 'iniciante',
      idade: 10,
      duracaoMaxima: 90,
      ordenacao: 'duracao',
      limite: 8
    });
    const params = new URLSearchParams(query);
    expect(params.get('busca')).toBe('horta');
    expect(params.get('tags')).toBe('pet,água');
    expect(params.get('idade')).toBe('10');
    expect(params.get('ordenacao')).toBe('duracao');
    expect(params.get('limite')).toBe('8');
  });

  it('remove filtros vazios', () => {
    expect(buildProjectQuery({ busca: '', tags: [], ordenacao: 'recentes' })).toBe('?ordenacao=recentes');
  });

  it('associa arquivos aos índices zero-based das listas corretas', () => {
    const values = validPublication();
    values.galeria = [
      { alt: 'Primeira imagem da galeria', arquivo: { ...coverFile, name: 'galeria.jpg' } }
    ];
    values.passos = [
      {
        titulo: 'Passo com imagem',
        corpo: 'Descrição completa do passo.',
        imagem: { ...coverFile, name: 'passo.jpg' }
      },
      { titulo: 'Passo sem imagem', corpo: 'Outra descrição completa.', imagem: null }
    ];
    values.baixaveis = [
      {
        rotulo: 'Manual em PDF',
        tipo: 'pdf',
        arquivo: { ...coverFile, name: 'manual.pdf', mimeType: 'application/pdf' }
      }
    ];
    values.arquivos = [
      {
        rotulo: 'Modelo 3D',
        tipo: 'stl',
        arquivo: { ...coverFile, name: 'modelo.stl', mimeType: 'model/stl' }
      }
    ];

    const parts = createPublicationParts(values);
    expect(parts.projeto.galeria).toEqual([{ alt: 'Primeira imagem da galeria', arquivoIndice: 0 }]);
    expect(parts.projeto.passos).toEqual([
      { titulo: 'Passo com imagem', corpo: 'Descrição completa do passo.', imagemArquivoIndice: 0 },
      { titulo: 'Passo sem imagem', corpo: 'Outra descrição completa.' }
    ]);
    expect(parts.projeto.baixaveis).toEqual([{ rotulo: 'Manual em PDF', tipo: 'pdf', arquivoIndice: 0 }]);
    expect(parts.projeto.arquivos).toEqual([{ rotulo: 'Modelo 3D', tipo: 'stl', arquivoIndice: 0 }]);
    expect(parts.passosImagens).toHaveLength(1);
  });

  it('usa exatamente os nomes de partes do controller multipart', () => {
    const appended: string[] = [];
    const valuesByName = new Map<string, unknown>();
    const OriginalFormData = globalThis.FormData;
    class TestFormData {
      append(name: string, value: unknown) {
        appended.push(name);
        valuesByName.set(name, value);
      }
    }
    globalThis.FormData = TestFormData as unknown as typeof FormData;

    const values = validPublication();
    values.galeria = [{ alt: 'Imagem adicional acessível', arquivo: coverFile }];
    values.passos[0]!.imagem = coverFile;
    values.baixaveis = [{ rotulo: 'Manual PDF', tipo: 'pdf', arquivo: coverFile }];
    values.arquivos = [{ rotulo: 'Modelo STL', tipo: 'stl', arquivo: coverFile }];
    buildPublicationFormData(values);

    expect(appended).toEqual(['projeto', 'capa', 'galeria', 'passosImagens', 'baixaveis', 'arquivos']);
    expect(typeof valuesByName.get('projeto')).toBe('string');
    expect(JSON.parse(valuesByName.get('projeto') as string)).toMatchObject({
      titulo: values.titulo,
      videoYoutube: values.videoYoutube,
      descricaoLonga: values.descricaoLonga
    });
    globalThis.FormData = OriginalFormData;
  });
});
