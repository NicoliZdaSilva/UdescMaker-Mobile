import type { ApiErrorPayload } from '../types/api';

const DEFAULT_API_URL = 'http://localhost:8080/api';
export const REQUEST_TIMEOUT_MS = 20_000;
export const PUBLICATION_REQUEST_TIMEOUT_MS = 5 * 60_000;

export interface ApiRequestInit extends RequestInit {
  timeoutMs?: number;
}

export class ApiClientError extends Error {
  constructor(
    message: string,
    public readonly status: number,
    public readonly code = 'ERRO_API',
    public readonly fields: Record<string, string> = {},
    public readonly path?: string
  ) {
    super(message);
    this.name = 'ApiClientError';
  }
}

export function getApiBaseUrl() {
  const configured = process.env.EXPO_PUBLIC_API_URL?.trim() || DEFAULT_API_URL;
  return configured.replace(/\/+$/, '');
}

function networkMessage(error: unknown) {
  if (error instanceof Error && error.name === 'AbortError') {
    return 'A API demorou para responder. Verifique sua conexão e tente novamente.';
  }
  return 'Não foi possível conectar à API. Verifique a conexão e a URL configurada.';
}

export async function requestJson<T>(
  path: string,
  init: ApiRequestInit = {}
): Promise<T> {
  const { timeoutMs = REQUEST_TIMEOUT_MS, ...fetchInit } = init;

  const controller = new AbortController();
  const timeout = setTimeout(() => controller.abort(), timeoutMs);

  const url = `${getApiBaseUrl()}${path}`;
  const method = fetchInit.method ?? 'GET';

  try {
    if (__DEV__) {
      console.log(`[API] Iniciando ${method} ${url}`);
    }

    const response = await fetch(url, {
      ...fetchInit,
      headers: {
        Accept: 'application/json',
        ...fetchInit.headers
      },
      signal: controller.signal
    });

    const responseText = await response.text();

    if (__DEV__) {
      console.log(`[API] ${method} ${url} -> HTTP ${response.status}`);

      if (responseText) {
        console.log(
          '[API] Resposta:',
          responseText.length > 3000
            ? `${responseText.slice(0, 3000)}...`
            : responseText
        );
      }
    }

    if (!response.ok) {
      let payload: ApiErrorPayload = {};

      try {
        payload = responseText
          ? (JSON.parse(responseText) as ApiErrorPayload)
          : {};
      } catch {
        // Mantém o status HTTP mesmo que a resposta não seja JSON.
      }

      throw new ApiClientError(
        payload.mensagem ||
          payload.erro ||
          `A API retornou o status ${response.status}.`,
        response.status,
        payload.codigo || 'ERRO_API',
        payload.campos || {},
        payload.path
      );
    }

    if (!responseText) {
      return undefined as T;
    }

    return JSON.parse(responseText) as T;
  } catch (error) {
    if (error instanceof ApiClientError) {
      if (__DEV__) {
        console.error('[API] Erro retornado pela API:', {
          url,
          method,
          status: error.status,
          code: error.code,
          message: error.message,
          fields: error.fields,
          path: error.path
        });
      }

      throw error;
    }

    if (__DEV__) {
      console.error('[API] Falha nativa do fetch:', {
        url,
        method,
        name: error instanceof Error ? error.name : undefined,
        message: error instanceof Error ? error.message : String(error),
        stack: error instanceof Error ? error.stack : undefined
      });
    }

    const detalhe =
      error instanceof Error && error.message
        ? ` Detalhe: ${error.name}: ${error.message}`
        : '';

    throw new ApiClientError(
      __DEV__
        ? `${networkMessage(error)}${detalhe}`
        : networkMessage(error),
      0,
      'SEM_CONEXAO'
    );
  } finally {
    clearTimeout(timeout);
  }
}
