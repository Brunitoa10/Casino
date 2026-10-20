const BASE_URL = import.meta.env.VITE_API_URL ?? '';
const TOKEN_KEY = 'aurum.token';

/** Error de la API con el código de negocio devuelto por el backend (ProblemDetail.code). */
export class ApiError extends Error {
  constructor(
    message: string,
    readonly status: number,
    readonly code: string,
  ) {
    super(message);
    this.name = 'ApiError';
  }
}

export const tokenStore = {
  get(): string | null {
    try {
      return localStorage.getItem(TOKEN_KEY);
    } catch {
      return null;
    }
  },
  set(token: string | null): void {
    try {
      if (token) localStorage.setItem(TOKEN_KEY, token);
      else localStorage.removeItem(TOKEN_KEY);
    } catch {
      /* almacenamiento no disponible: la sesión vive solo en memoria */
    }
  },
};

type Listener = () => void;
const unauthorizedListeners = new Set<Listener>();

/** Permite a la capa de sesión reaccionar a un 401 en cualquier llamada. */
export function onUnauthorized(listener: Listener): () => void {
  unauthorizedListeners.add(listener);
  return () => unauthorizedListeners.delete(listener);
}

export async function request<T>(path: string, init: { method?: string; body?: unknown } = {}): Promise<T> {
  const headers: Record<string, string> = { Accept: 'application/json' };
  const token = tokenStore.get();
  if (token) headers.Authorization = `Bearer ${token}`;
  if (init.body !== undefined) headers['Content-Type'] = 'application/json';

  let response: Response;
  try {
    response = await fetch(`${BASE_URL}${path}`, {
      method: init.method ?? 'GET',
      headers,
      body: init.body !== undefined ? JSON.stringify(init.body) : undefined,
    });
  } catch {
    throw new ApiError('No se pudo conectar con el servidor', 0, 'NETWORK_ERROR');
  }

  if (response.status === 204) return undefined as T;

  const data = await response.json().catch(() => null);
  if (!response.ok) {
    if (response.status === 401) unauthorizedListeners.forEach((l) => l());
    if (!data) {
      // Sin cuerpo JSON: suele ser el proxy de Vite sin backend detrás
      throw new ApiError('No hay respuesta del servidor. ¿Está corriendo el backend en el puerto 8080?', response.status, 'SERVER_UNAVAILABLE');
    }
    const message = data.detail ?? data.message ?? `Error del servidor (${response.status})`;
    throw new ApiError(message, response.status, data.code ?? 'UNKNOWN');
  }
  return data as T;
}
