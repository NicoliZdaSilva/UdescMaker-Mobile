import type { ProjectFilters } from '../types/api';

export function createEmptyFilters(): ProjectFilters {
  return { ordenacao: 'recentes' };
}

export function parseTagFilter(value: string) {
  return [...new Set(value.split(',').map((tag) => tag.trim()).filter(Boolean))];
}

export function applyDraftFilters(filters: ProjectFilters, tagText: string): ProjectFilters {
  return { ...filters, tags: parseTagFilter(tagText), ordenacao: filters.ordenacao ?? 'recentes' };
}
