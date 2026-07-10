import { act, renderHook, waitFor } from '@testing-library/react-native';

import { useAsyncResource } from './useAsyncResource';

function deferred<T>() {
  let resolve!: (value: T) => void;
  let reject!: (reason: unknown) => void;
  const promise = new Promise<T>((resolvePromise, rejectPromise) => {
    resolve = resolvePromise;
    reject = rejectPromise;
  });
  return { promise, resolve, reject };
}

describe('useAsyncResource', () => {
  it('ignora resultado antigo que termina depois da requisição mais recente', async () => {
    const first = deferred<string>();
    const second = deferred<string>();

    const { result, rerender } = renderHook(
      ({ loader, dependency }: { loader: () => Promise<string>; dependency: number }) =>
        useAsyncResource(loader, [dependency]),
      { initialProps: { loader: () => first.promise, dependency: 1 } }
    );

    rerender({ loader: () => second.promise, dependency: 2 });
    await act(async () => second.resolve('mais recente'));
    await waitFor(() => expect(result.current.data).toBe('mais recente'));

    await act(async () => first.resolve('obsoleto'));
    await waitFor(() => expect(result.current.data).toBe('mais recente'));
  });
});
