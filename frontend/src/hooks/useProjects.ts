import { useCallback } from 'react';

import { getProject, getTaxonomy, listProjects } from '../services/projectsApi';
import type { ProjectFilters } from '../types/api';
import { useCatalog } from './CatalogContext';
import { useAsyncResource } from './useAsyncResource';

export function useProjects(filters: ProjectFilters) {
  const { version } = useCatalog();
  const serialized = JSON.stringify(filters);
  const loader = useCallback(() => listProjects(filters), [serialized]);
  return useAsyncResource(loader, [loader, version]);
}

export function useProject(slug: string) {
  const { version } = useCatalog();
  const loader = useCallback(() => getProject(slug), [slug]);
  return useAsyncResource(loader, [loader, version]);
}

export function useTaxonomy() {
  const loader = useCallback(() => getTaxonomy(), []);
  return useAsyncResource(loader, [loader]);
}
