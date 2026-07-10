import { useCallback, useEffect, useRef, useState } from 'react';

interface AsyncResource<T> {
  data: T | null;
  error: Error | null;
  loading: boolean;
  refreshing: boolean;
  reload: () => void;
}

export function useAsyncResource<T>(loader: () => Promise<T>, dependencies: unknown[]): AsyncResource<T> {
  const mounted = useRef(true);
  const generation = useRef(0);
  const [data, setData] = useState<T | null>(null);
  const [error, setError] = useState<Error | null>(null);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [reloadKey, setReloadKey] = useState(0);

  const reload = useCallback(() => setReloadKey((key) => key + 1), []);

  useEffect(() => {
    mounted.current = true;
    const currentGeneration = ++generation.current;
    const isCurrent = () => mounted.current && currentGeneration === generation.current;
    const hasData = data != null;
    if (hasData) setRefreshing(true);
    else setLoading(true);
    setError(null);

    loader()
      .then((result) => {
        if (isCurrent()) setData(result);
      })
      .catch((reason: unknown) => {
        if (isCurrent()) {
          setError(reason instanceof Error ? reason : new Error('Erro inesperado.'));
        }
      })
      .finally(() => {
        if (isCurrent()) {
          setLoading(false);
          setRefreshing(false);
        }
      });

    return () => {
      if (currentGeneration === generation.current) {
        generation.current += 1;
      }
    };
    // O chamador controla a identidade de loader pelas dependências explícitas.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [...dependencies, reloadKey]);

  return { data, error, loading, refreshing, reload };
}
