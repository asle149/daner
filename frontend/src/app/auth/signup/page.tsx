'use client';

import { Suspense, useEffect, useMemo, useState } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import { useMutation, useQuery } from '@tanstack/react-query';
import { checkNickname, signup } from '@/lib/api/endpoints';
import { ApiError } from '@/lib/api/client';
import { useAuth } from '@/lib/auth/AuthContext';

const NICKNAME_PATTERN = /^[a-zA-Z0-9가-힣]+$/;

function validateLocally(nickname: string): string | null {
  if (!nickname) return null;
  if (nickname.length < 2 || nickname.length > 12) return '닉네임은 2~12자여야 해요.';
  if (!NICKNAME_PATTERN.test(nickname)) return '한글, 영문, 숫자만 가능해요.';
  return null;
}

function useDebounced<T>(value: T, delayMs: number): T {
  const [debounced, setDebounced] = useState(value);
  useEffect(() => {
    const handle = setTimeout(() => setDebounced(value), delayMs);
    return () => clearTimeout(handle);
  }, [value, delayMs]);
  return debounced;
}

function SignupInner() {
  const router = useRouter();
  const params = useSearchParams();
  const { setSession } = useAuth();
  const signupToken = params.get('signup_token') ?? '';

  const [nickname, setNickname] = useState('');
  const debouncedNickname = useDebounced(nickname, 300);
  const localError = useMemo(() => validateLocally(nickname), [nickname]);

  const nicknameQuery = useQuery({
    queryKey: ['nickname-check', debouncedNickname],
    queryFn: () => checkNickname(debouncedNickname),
    enabled: !!debouncedNickname && validateLocally(debouncedNickname) === null,
    staleTime: 0,
  });

  const submit = useMutation({
    mutationFn: () => signup(signupToken, nickname),
    onSuccess: (tokens) => {
      setSession(tokens.user, tokens.accessToken, tokens.refreshToken);
      router.replace('/');
    },
  });

  if (!signupToken) {
    return (
      <main className="flex flex-1 flex-col items-center justify-center gap-4 px-6 text-center">
        <p className="text-secondary">가입 토큰이 없어요. 다시 로그인해주세요.</p>
        <a href="/" className="underline">홈으로</a>
      </main>
    );
  }

  const remoteError =
    nicknameQuery.data && !nicknameQuery.data.available ? nicknameQuery.data.reason : null;
  const submitError = submit.error instanceof ApiError ? submit.error.message : null;
  const isSubmittable =
    !!nickname &&
    !localError &&
    nicknameQuery.data?.available === true &&
    !submit.isPending;

  return (
    <main className="flex flex-1 flex-col items-center justify-center px-6">
      <div className="w-full max-w-sm space-y-6 text-center">
        <h1 className="text-[22px]">어떻게 불러드릴까요?</h1>
        <p className="text-sm text-secondary">한 번 정하면 바꾸기 어려워요.</p>
        <input
          autoFocus
          className="input-underline text-center text-lg"
          placeholder="닉네임"
          value={nickname}
          onChange={(e) => setNickname(e.target.value.trim())}
          maxLength={12}
        />
        <div className="min-h-5 text-sm text-tertiary">
          {localError ? localError : remoteError ? remoteError : nicknameQuery.data?.available ? '쓸 수 있어요' : ''}
        </div>
        <button
          type="button"
          className="w-full border-b border-foreground py-2 text-sm disabled:opacity-40"
          disabled={!isSubmittable}
          onClick={() => submit.mutate()}
        >
          {submit.isPending ? '가입 중…' : '시작하기'}
        </button>
        {submitError ? <p className="text-sm text-accent">{submitError}</p> : null}
      </div>
    </main>
  );
}

export default function SignupPage() {
  return (
    <Suspense fallback={<main className="flex flex-1 items-center justify-center text-secondary">불러오는 중…</main>}>
      <SignupInner />
    </Suspense>
  );
}
