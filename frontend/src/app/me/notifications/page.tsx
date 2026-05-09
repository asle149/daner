'use client';

import { useEffect, useMemo } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { useInfiniteQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Header } from '@/components/ui/Header';
import { fetchNotifications, markNotificationsRead } from '@/lib/api/endpoints';
import { useAuth } from '@/lib/auth/AuthContext';
import { timeAgo } from '@/lib/util/timeAgo';
import type { Notification } from '@/types/api';

export default function NotificationsPage() {
  const router = useRouter();
  const queryClient = useQueryClient();
  const { loading, isAuthenticated } = useAuth();

  useEffect(() => {
    if (!loading && !isAuthenticated) router.replace('/');
  }, [loading, isAuthenticated, router]);

  const list = useInfiniteQuery({
    queryKey: ['notifications'],
    queryFn: ({ pageParam }) => fetchNotifications(pageParam ?? null),
    initialPageParam: null as string | null,
    getNextPageParam: (last) => last.nextCursor,
    enabled: isAuthenticated,
  });

  const notifications: Notification[] = useMemo(
    () => list.data?.pages.flatMap((p) => p.notifications) ?? [],
    [list.data],
  );

  const markRead = useMutation({
    mutationFn: (ids: number[]) => markNotificationsRead(ids),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['notifications-unread'] });
    },
  });

  // 첫 페이지 로드되면 안 읽은 알림을 자동 읽음 처리
  useEffect(() => {
    if (!notifications.length || markRead.isPending) return;
    const unreadIds = notifications.filter((n) => !n.isRead).map((n) => n.id);
    if (unreadIds.length > 0) markRead.mutate(unreadIds);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [notifications.length]);

  // 단어로 그룹화
  const groups = useMemo(() => {
    const m = new Map<string, Notification[]>();
    for (const n of notifications) {
      const arr = m.get(n.word) ?? [];
      arr.push(n);
      m.set(n.word, arr);
    }
    return Array.from(m.entries());
  }, [notifications]);

  return (
    <>
      <Header />
      <main className="mx-auto flex w-full max-w-2xl flex-1 flex-col px-6 pb-12">
        <h1 className="mt-12 text-center font-display text-3xl font-bold">받은 마음</h1>

        {list.isLoading ? (
          <p className="mt-12 text-center font-display text-sm text-tertiary">불러오는 중…</p>
        ) : groups.length === 0 ? (
          <p className="mt-12 text-center font-display text-sm text-tertiary">
            아직 받은 알림이 없어요
          </p>
        ) : (
          <div className="mt-10 space-y-8">
            {groups.map(([word, items]) => (
              <section key={word}>
                <Link
                  href={`/words/${encodeURIComponent(word)}`}
                  className="font-display text-[12px] tracking-widest text-tertiary"
                >
                  {word}
                </Link>
                <div className="mt-2 space-y-3">
                  {items.map((n) => (
                    <NotificationRow key={n.id} n={n} />
                  ))}
                </div>
              </section>
            ))}
          </div>
        )}

        {list.hasNextPage ? (
          <button
            type="button"
            onClick={() => void list.fetchNextPage()}
            className="mt-6 w-full py-2 font-display text-xs text-tertiary"
            disabled={list.isFetchingNextPage}
          >
            {list.isFetchingNextPage ? '불러오는 중…' : '더 보기'}
          </button>
        ) : null}
      </main>
    </>
  );
}

function NotificationRow({ n }: { n: Notification }) {
  const actor = n.actor.nickname ?? n.actor.label ?? '누군가';
  const dimmed = n.isRead ? 'opacity-60' : '';
  return (
    <div className={`font-display text-base leading-relaxed ${dimmed}`}>
      {n.type === 'reply' ? (
        <>
          <p>{actor}님이 답글을 남겼어요</p>
          {n.preview ? <p className="text-secondary">&ldquo;{n.preview}&rdquo;</p> : null}
        </>
      ) : (
        <p>{actor}님이 ♡를 눌렀어요</p>
      )}
      <p className="text-[12px] text-tertiary">{timeAgo(n.createdAt)}</p>
    </div>
  );
}
