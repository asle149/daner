// localStorage 래퍼. SSR에선 noop이므로 'use client' 컴포넌트에서만 호출.

const ACCESS_KEY = 'daner.access_token';
const REFRESH_KEY = 'daner.refresh_token';
const ANON_KEY = 'daner.anonymous_token';

const isBrowser = () => typeof window !== 'undefined';

export const tokenStorage = {
  getAccess(): string | null {
    return isBrowser() ? localStorage.getItem(ACCESS_KEY) : null;
  },
  getRefresh(): string | null {
    return isBrowser() ? localStorage.getItem(REFRESH_KEY) : null;
  },
  setTokens(access: string, refresh: string) {
    if (!isBrowser()) return;
    localStorage.setItem(ACCESS_KEY, access);
    localStorage.setItem(REFRESH_KEY, refresh);
  },
  setAccess(access: string) {
    if (!isBrowser()) return;
    localStorage.setItem(ACCESS_KEY, access);
  },
  clear() {
    if (!isBrowser()) return;
    localStorage.removeItem(ACCESS_KEY);
    localStorage.removeItem(REFRESH_KEY);
  },
  getOrCreateAnonymous(): string {
    if (!isBrowser()) return '';
    let token = localStorage.getItem(ANON_KEY);
    if (!token) {
      token = crypto.randomUUID();
      localStorage.setItem(ANON_KEY, token);
    }
    return token;
  },
};
