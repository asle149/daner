'use client';

import { useEffect, useState, type FormEvent } from 'react';
import { useRouter } from 'next/navigation';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { Header } from '@/components/ui/Header';
import { wipeWordRoom } from '@/lib/api/endpoints';
import { ApiError } from '@/lib/api/client';
import { useAuth } from '@/lib/auth/AuthContext';
import { normalizeForRouting } from '@/lib/util/normalizeWord';

export default function AdminPage() {
  const router = useRouter();
  const queryClient = useQueryClient();
  const { loading, isAuthenticated, isAdmin } = useAuth();

  const [target, setTarget] = useState('');
  const [result, setResult] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  // 비관리자는 홈으로 돌려보냄
  useEffect(() => {
    if (loading) return;
    if (!isAuthenticated || !isAdmin) router.replace('/');
  }, [loading, isAuthenticated, isAdmin, router]);

  const wipe = useMutation({
    mutationFn: (word: string) => wipeWordRoom(word),
    onSuccess: (data) => {
      setResult(`'${data.word}' 단어 방에서 ${data.removed}개 글을 지웠어요.`);
      setTarget('');
      void queryClient.invalidateQueries({ queryKey: ['comments'] });
      void queryClient.invalidateQueries({ queryKey: ['word'] });
      void queryClient.invalidateQueries({ queryKey: ['home'] });
    },
    onError: (err) => {
      setError(err instanceof ApiError ? err.message : '지우지 못했어요.');
    },
  });

  const onSubmit = (e: FormEvent) => {
    e.preventDefault();
    setResult(null);
    setError(null);
    let word: string;
    try {
      word = normalizeForRouting(target);
    } catch (err) {
      setError(err instanceof Error ? err.message : '단어가 올바르지 않아요.');
      return;
    }
    if (!word) {
      setError('단어를 입력해주세요.');
      return;
    }
    if (!window.confirm(`'${word}' 의 모든 댓글/답글을 지웁니다. 되돌릴 수 없어요.`)) return;
    wipe.mutate(word);
  };

  if (loading || !isAuthenticated || !isAdmin) return null;

  return (
    <>
      <Header />
      <main className="mx-auto flex w-full max-w-xl flex-1 flex-col px-6 pb-16">
        <h1 className="mt-12 text-center font-display text-3xl font-bold">관리자</h1>
        <p className="mt-2 text-center font-display text-sm text-tertiary">
          단어 방 비우기 — 그 단어의 모든 글이 사라집니다
        </p>

        <form onSubmit={onSubmit} className="mt-12">
          <label className="block font-display text-sm text-secondary">비울 단어</label>
          <input
            className="input-underline mt-2 font-display text-lg"
            value={target}
            onChange={(e) => {
              setTarget(e.target.value);
              if (error) setError(null);
              if (result) setResult(null);
            }}
            placeholder="예: 페르소나"
            disabled={wipe.isPending}
            autoFocus
          />
          <div className="mt-4 flex justify-end">
            <button
              type="submit"
              className="font-display text-sm text-accent disabled:text-tertiary"
              disabled={wipe.isPending || !target.trim()}
            >
              {wipe.isPending ? '지우는 중…' : '비우기'}
            </button>
          </div>
        </form>

        <p className="mt-6 min-h-5 text-center font-display text-sm">
          {error ? <span className="text-accent">{error}</span> : null}
          {result ? <span className="text-secondary">{result}</span> : null}
        </p>
      </main>
    </>
  );
}
