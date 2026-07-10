import { applyDraftFilters, createEmptyFilters, parseTagFilter } from './filters';

describe('filtros', () => {
  it('aplica e normaliza tags do rascunho', () => {
    expect(
      applyDraftFilters({ busca: 'horta', categoria: 'sustentabilidade' }, 'pet, água, pet')
    ).toEqual({
      busca: 'horta',
      categoria: 'sustentabilidade',
      tags: ['pet', 'água'],
      ordenacao: 'recentes'
    });
    expect(parseTagFilter('')).toEqual([]);
  });

  it('limpa todos os filtros mantendo a ordenação padrão', () => {
    expect(createEmptyFilters()).toEqual({ ordenacao: 'recentes' });
  });
});
