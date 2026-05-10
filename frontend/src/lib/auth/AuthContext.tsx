'use client';

import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from 'react';
import { tokenStorage } from './tokens';
import { apiFetch } from '@/lib/api/client';
import { logout as logoutEndpoint } from '@/lib/api/endpoints';
import type { User } from '@/types/api';

type AuthState = {
  user: User | null;
  loading: boolean;
  isAuthenticated: boolean;
  isAdmin: boolean;
  refresh: () => Promise<void>;
  setSession: (user: User, accessToken: string, refreshToken: string) => void;
  logout: () => Promise<void>;
};

const AuthContext = createContext<AuthState | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);

  const refresh = useCallback(async () => {
    const access = tokenStorage.getAccess();
    if (!access) {
      setUser(null);
      setLoading(false);
      return;
    }
    try {
      // /users/me 가 본인 프로필을 돌려주므로 user를 거기서 추출.
      const profile = await apiFetch<{ user: User }>('/users/me');
      setUser(profile.user);
    } catch {
      // 토큰 만료/실패 시 비로그인 상태로
      tokenStorage.clear();
      setUser(null);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void refresh();
  }, [refresh]);

  const setSession = useCallback((nextUser: User, accessToken: string, refreshToken: string) => {
    tokenStorage.setTokens(accessToken, refreshToken);
    setUser(nextUser);
  }, []);

  const logout = useCallback(async () => {
    try {
      await logoutEndpoint();
    } catch {
      // 토큰이 이미 무효해도 진행
    }
    tokenStorage.clear();
    setUser(null);
  }, []);

  const value = useMemo<AuthState>(() => ({
    user,
    loading,
    isAuthenticated: user !== null,
    isAdmin: user?.isAdmin === true,
    refresh,
    setSession,
    logout,
  }), [user, loading, refresh, setSession, logout]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthState {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}
