'use client';

import { useEffect, useState, type FormEvent } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Header } from '@/components/ui/Header';
import { fetchAdminStats, wipeWordRoom } from '@/lib/api/endpoints';
import { ApiError } from '@/lib/api/client';
import { useAuth } from '@/lib/auth/AuthContext';
import { normalizeForRouting } from '@/lib/util/normalizeWord';
import { timeAgo } from '@/lib/util/timeAgo';

export default function AdminPage() {
  const router = useRouter();
  const queryClient = useQueryClient();
  const { loading, isAuthenticated, isAdmin } = useAuth();

  const [target, setTarget] = useState('');
  const [result, setResult] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (loading) return;
    if (!isAuthenticated || !isAdmin) router.replace('/');
  }, [loading, isAuthenticated, isAdmin, router]);

  const stats = useQuery({
    queryKey: ['admin-stats'],
    queryFn: fetchAdminStats,
    enabled: isAuthenticated && isAdmin,
    refetchOnWindowFocus: false,
  });

  const wipe = useMutation({
    mutationFn: (word: string) => wipeWordRoom(word),
    onSuccess: (data) => {
      setResult(`'${data.word}' 단어 방에서 ${data.removed}개 글을 지웠어요.`);
      setTarget('');
      void queryClient.invalidateQueries({ queryKey: ['comments'] });
      void queryClient.invalidateQueries({ queryKey: ['word'] });
      void queryClient.invalidateQueries({ queryKey: ['home'] });
      void queryClient.invalidateQueries({ queryKey: ['admin-stats'] });
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

  const s = stats.data;

  return (
    <>
      <Header />
      <main className="mx-auto flex w-full max-w-3xl flex-1 flex-col px-6 pb-16">
        <h1 className="mt-12 text-center font-display text-3xl font-bold">관리자</h1>
        <p className="mt-2 text-center font-display text-sm text-tertiary">
          사이트 통계와 운영 도구
        </p>

        {/* 누적 카드 */}
        <section className="mt-10">
          <h2 className="font-display text-xs tracking-widest text-tertiary">전체</h2>
          <div className="mt-3 grid grid-cols-2 gap-3 sm:grid-cols-4">
            <StatCard label="회원" value={s?.totals.users} />
            <StatCard label="단어 방" value={s?.totals.words} />
            <StatCard label="댓글" value={s?.totals.comments} />
            <StatCard label="익명 댓글" value={s?.totals.anonymousComments} />
          </div>
        </section>

        {/* 오늘 카드 */}
        <section className="mt-8">
          <h2 className="font-display text-xs tracking-widest text-tertiary">오늘 (KST)</h2>
          <div className="mt-3 grid grid-cols-3 gap-3">
            <StatCard label="새 단어" value={s?.today.newWords} />
            <StatCard label="새 댓글" value={s?.today.newComments} />
            <StatCard label="새 가입" value={s?.today.newUsers} />
          </div>
        </section>

        {/* 리스트들 */}
        <section className="mt-10 grid gap-8 sm:grid-cols-2">
          <ListBlock title="오늘 새로 생긴 단어 방" empty="아직 없어요">
            {s?.newWordsToday.map((w) => (
              <li key={w.id} className="flex items-baseline justify-between">
                <Link
                  href={`/words/${encodeURIComponent(w.word)}`}
                  className="font-display text-base text-foreground hover:text-accent"
                >
                  {w.word}
                </Link>
                <span className="font-display text-[12px] text-tertiary">
                  {w.commentCount}개 · {timeAgo(w.createdAt)}
                </span>
              </li>
            ))}
          </ListBlock>

          <ListBlock title="오늘 활발한 단어 TOP 5" empty="아직 없어요">
            {s?.topActiveWordsToday.map((w) => (
              <li key={w.id} className="flex items-baseline justify-between">
                <Link
                  href={`/words/${encodeURIComponent(w.word)}`}
                  className="font-display text-base text-foreground hover:text-accent"
                >
                  {w.word}
                </Link>
                <span className="font-display text-[12px] text-tertiary">
                  오늘 {w.commentCount}개
                </span>
              </li>
            ))}
          </ListBlock>

          <ListBlock title="최근 가입" empty="없음">
            {s?.recentUsers.map((u) => (
              <li key={u.id} className="flex items-baseline justify-between">
                <span className="font-display text-base text-foreground">
                  {u.nickname}
                  {u.isAdmin ? (
                    <span className="ml-2 text-[11px] tracking-widest text-accent">ADMIN</span>
                  ) : null}
                </span>
                <span className="font-display text-[12px] text-tertiary">{timeAgo(u.createdAt)}</span>
              </li>
            ))}
          </ListBlock>

          <ListBlock title="최근 관리자 작업" empty="기록 없음">
            {s?.recentAudits.map((a) => (
              <li key={a.id} className="font-display text-[13px]">
                <span className="text-foreground">{actionLabel(a.action)}</span>
                {a.detail ? (
                  <span className="ml-1 text-tertiary">· {a.detail}</span>
                ) : null}
                <div className="text-[11px] text-tertiary">
                  관리자 #{a.adminId} · {timeAgo(a.createdAt)}
                </div>
              </li>
            ))}
          </ListBlock>
        </section>

        {/* 운영 도구 */}
        <section className="mt-12 border-t border-dashed border-hairline pt-8">
          <h2 className="font-display text-xs tracking-widest text-tertiary">운영 도구</h2>
          <form onSubmit={onSubmit} className="mt-4">
            <label className="block font-display text-sm text-secondary">
              단어 방 비우기 — 그 단어의 모든 글이 사라집니다
            </label>
            <div className="mt-2 flex items-end gap-3">
              <input
                className="input-underline flex-1 font-display text-lg"
                value={target}
                onChange={(e) => {
                  setTarget(e.target.value);
                  if (error) setError(null);
                  if (result) setResult(null);
                }}
                placeholder="예: 페르소나"
                disabled={wipe.isPending}
              />
              <button
                type="submit"
                className="font-display text-sm text-accent disabled:text-tertiary"
                disabled={wipe.isPending || !target.trim()}
              >
                {wipe.isPending ? '지우는 중…' : '비우기'}
              </button>
            </div>
          </form>
          <p className="mt-3 min-h-5 font-display text-sm">
            {error ? <span className="text-accent">{error}</span> : null}
            {result ? <span className="text-secondary">{result}</span> : null}
          </p>
        </section>
      </main>
    </>
  );
}

function StatCard({ label, value }: { label: string; value: number | undefined }) {
  return (
    <div className="rounded-sm border border-hairline px-4 py-3 text-center">
      <div className="font-display text-[11px] tracking-widest text-tertiary">{label}</div>
      <div className="mt-1 font-display text-2xl font-bold text-foreground">
        {value === undefined ? '…' : value.toLocaleString()}
      </div>
    </div>
  );
}

function ListBlock({
  title,
  empty,
  children,
}: {
  title: string;
  empty: string;
  children: React.ReactNode;
}) {
  const arr = Array.isArray(children) ? children : [children];
  const hasItems = arr.some((c) => c != null && c !== false);
  return (
    <div>
      <h3 className="font-display text-xs tracking-widest text-tertiary">{title}</h3>
      <ul className="mt-3 space-y-2 border-t border-dashed border-hairline pt-3">
        {hasItems ? (
          children
        ) : (
          <li className="font-display text-[13px] text-tertiary">{empty}</li>
        )}
      </ul>
    </div>
  );
}

function actionLabel(action: string): string {
  switch (action) {
    case 'DELETE_COMMENT':
      return '댓글 삭제';
    case 'WIPE_WORD':
      return '단어 방 비움';
    default:
      return action;
  }
}
