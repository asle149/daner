import { tokenStorage } from '@/lib/auth/tokens';
import type { ApiResponse } from '@/types/api';

const BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL ?? 'http://localhost:8080/v1';

export class ApiError extends Error {
  readonly code: string;
  readonly status: number;

  constructor(code: string, message: string, status: number) {
    super(message);
    this.code = code;
    this.status = status;
  }
}

type RequestOptions = RequestInit & {
  attachAuth?: boolean;       // 기본 true
  attachAnonymous?: boolean;  // 비회원 댓글용. 기본 false
};

let refreshInFlight: Promise<string | null> | null = null;

async function tryRefreshAccessToken(): Promise<string | null> {
  if (refreshInFlight) return refreshInFlight;
  const refresh = tokenStorage.getRefresh();
  if (!refresh) return null;

  refreshInFlight = (async () => {
    try {
      const res = await fetch(`${BASE_URL}/auth/refresh`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ refreshToken: refresh }),
      });
      if (!res.ok) {
        tokenStorage.clear();
        return null;
      }
      const json = (await res.json()) as ApiResponse<{ accessToken: string }>;
      if (!json.success) {
        tokenStorage.clear();
        return null;
      }
      tokenStorage.setAccess(json.data.accessToken);
      return json.data.accessToken;
    } finally {
      refreshInFlight = null;
    }
  })();

  return refreshInFlight;
}

export async function apiFetch<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const { attachAuth = true, attachAnonymous = false, headers, ...rest } = options;
  const url = path.startsWith('http') ? path : `${BASE_URL}${path}`;

  const buildHeaders = (token: string | null): HeadersInit => {
    const h: Record<string, string> = {
      ...((headers as Record<string, string>) ?? {}),
    };
    if (rest.body && !h['Content-Type']) h['Content-Type'] = 'application/json';
    if (attachAuth && token) h['Authorization'] = `Bearer ${token}`;
    if (attachAnonymous && !token) h['X-Anonymous-Token'] = tokenStorage.getOrCreateAnonymous();
    return h;
  };

  const performRequest = async (token: string | null): Promise<Response> => {
    return fetch(url, { ...rest, headers: buildHeaders(token) });
  };

  let token = attachAuth ? tokenStorage.getAccess() : null;
  let response = await performRequest(token);

  if (response.status === 401 && attachAuth && tokenStorage.getRefresh()) {
    const refreshed = await tryRefreshAccessToken();
    if (refreshed) {
      token = refreshed;
      response = await performRequest(token);
    }
  }

  let body: ApiResponse<T> | null = null;
  try {
    body = (await response.json()) as ApiResponse<T>;
  } catch {
    if (!response.ok) {
      throw new ApiError('NETWORK', `요청 실패 (${response.status})`, response.status);
    }
    throw new ApiError('PARSE', '응답을 읽을 수 없습니다.', response.status);
  }

  if (!body.success) {
    throw new ApiError(body.error.code, body.error.message, response.status);
  }
  return body.data;
}
