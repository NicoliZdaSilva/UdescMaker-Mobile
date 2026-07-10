import { createContext, type PropsWithChildren, useCallback, useContext, useMemo, useState } from 'react';

interface CatalogContextValue {
  version: number;
  invalidateCatalog: () => void;
}

const CatalogContext = createContext<CatalogContextValue | null>(null);

export function CatalogProvider({ children }: PropsWithChildren) {
  const [version, setVersion] = useState(0);
  const invalidateCatalog = useCallback(() => setVersion((current) => current + 1), []);
  const value = useMemo(() => ({ version, invalidateCatalog }), [invalidateCatalog, version]);

  return <CatalogContext.Provider value={value}>{children}</CatalogContext.Provider>;
}

export function useCatalog() {
  const context = useContext(CatalogContext);
  if (!context) throw new Error('useCatalog deve ser usado dentro de CatalogProvider.');
  return context;
}
