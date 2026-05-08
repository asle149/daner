'use client';

import { Suspense, useEffect } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import { tokenStorage } from '@/lib/auth/tokens';
import { useAuth } from '@/lib/auth/AuthContext';

function AuthSuccessInner() {
  const router = useRouter();
  const params = useSearchParams();
  const { refresh } = useAuth();

  useEffect(() => {
    const access = params.get('access_token');
    const refreshToken = params.get('refresh_token');
    if (!access || !refreshToken) {
      router.replace('/');
      return;
    }
    tokenStorage.setTokens(access, refreshToken);
    void refresh().finally(() => router.replace('/'));
  }, [params, router, refresh]);

  return (
    <main className="flex flex-1 items-center justify-center text-secondary">
      로그인하는 중…
    </main>
  );
}

export default function AuthSuccessPage() {
  return (
    <Suspense fallback={<main className="flex flex-1 items-center justify-center text-secondary">로그인하는 중…</main>}>
      <AuthSuccessInner />
    </Suspense>
  );
}
