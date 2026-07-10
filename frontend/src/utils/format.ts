import type { DifficultyId } from '../types/api';

const difficultyLabels: Record<DifficultyId, string> = {
  iniciante: 'Iniciante',
  intermediario: 'Intermediário',
  avancado: 'Avançado'
};

export function formatDate(date: string) {
  const [year, month, day] = date.slice(0, 10).split('-').map(Number);
  if (!year || !month || !day) {
    return date;
  }
  return new Intl.DateTimeFormat('pt-BR').format(new Date(year, month - 1, day));
}

export function formatDuration(minutes: number) {
  if (minutes < 60) return `${minutes} min`;
  const hours = Math.floor(minutes / 60);
  const remainder = minutes % 60;
  return remainder ? `${hours}h${String(remainder).padStart(2, '0')}` : `${hours}h`;
}

export function difficultyLabel(difficulty: DifficultyId) {
  return difficultyLabels[difficulty];
}

export function compactFilters(filters: object) {
  return Object.fromEntries(
    Object.entries(filters).filter(([, value]) => {
      if (value == null || value === '') return false;
      return !Array.isArray(value) || value.length > 0;
    })
  );
}
