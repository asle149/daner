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

type Bucket =
  | { kind: 'reply'; key: string; word: string; commentId: number; rep: Notification; ids: number[] }
  | { kind: 'like'; key: string; word: string; commentId: number; rep: Notification; ids: number[]; count: number };

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

  useEffect(() => {
    if (!notifications.length || markRead.isPending) return;
    const unreadIds = notifications.filter((n) => !n.isRead).map((n) => n.id);
    if (unreadIds.length > 0) markRead.mutate(unreadIds);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [notifications.length]);

  // 답글은 개별 알림, 같은 댓글의 좋아요는 합쳐서 한 줄로
  const buckets: Bucket[] = useMemo(() => {
    const out: Bucket[] = [];
    const likeBucketByComment = new Map<number, Bucket & { kind: 'like' }>();
    for (const n of notifications) {
      if (n.type === 'reply') {
        out.push({
          kind: 'reply',
          key: `r-${n.id}`,
          word: n.word,
          commentId: n.commentId,
          rep: n,
          ids: [n.id],
        });
        continue;
      }
      const existing = likeBucketByComment.get(n.commentId);
      if (existing) {
        existing.ids.push(n.id);
        existing.count += 1;
        // 가장 최신을 대표로
        if (n.createdAt > existing.rep.createdAt) existing.rep = n;
      } else {
        const b: Bucket & { kind: 'like' } = {
          kind: 'like',
          key: `l-${n.commentId}`,
          word: n.word,
          commentId: n.commentId,
          rep: n,
          ids: [n.id],
          count: 1,
        };
        likeBucketByComment.set(n.commentId, b);
        out.push(b);
      }
    }
    return out;
  }, [notifications]);

  // 단어로 그룹
  const groups = useMemo(() => {
    const m = new Map<string, Bucket[]>();
    for (const b of buckets) {
      const arr = m.get(b.word) ?? [];
      arr.push(b);
      m.set(b.word, arr);
    }
    return Array.from(m.entries());
  }, [buckets]);

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
                  {items.map((b) => (
                    <NotificationRow key={b.key} bucket={b} />
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

function NotificationRow({ bucket }: { bucket: Bucket }) {
  const n = bucket.rep;
  const dimmed = n.isRead ? 'opacity-60' : '';
  // 답글이면 부모 댓글 ID를 ?p= 로 같이 넘겨야 도착 후 답글 목록이 자동으로 펼쳐짐.
  const params = bucket.kind === 'reply' && n.parentCommentId
    ? `?p=${n.parentCommentId}`
    : '';
  const href = `/words/${encodeURIComponent(n.word)}${params}#comment-${n.commentId}`;

  let headline: string;
  if (bucket.kind === 'reply') {
    // 답글: 익명이면 "누군가가", 비익명이면 "{닉네임}님이"
    const name = n.actor.nickname;
    headline = name ? `${name}님이 답글을 남겼어요` : '누군가가 답글을 남겼어요';
  } else {
    // 좋아요: actor 숨김. 1개면 "누군가가", 2명 이상이면 "N명이"
    headline = bucket.count >= 2
      ? `${bucket.count}명이 ♡를 눌렀어요`
      : '누군가가 ♡를 눌렀어요';
  }

  return (
    <Link
      href={href}
      className={`block rounded-sm font-display text-base leading-relaxed transition-colors hover:bg-hairline/30 ${dimmed}`}
    >
      <p>{headline}</p>
      {n.commentPreview ? (
        <p className="text-sm text-tertiary">{ellipsize(n.commentPreview, 60)}</p>
      ) : null}
      {bucket.kind === 'reply' && n.preview ? (
        <p className="text-sm text-secondary">{ellipsize(n.preview, 60)}</p>
      ) : null}
      <p className="text-[12px] text-tertiary">{timeAgo(n.createdAt)}</p>
    </Link>
  );
}

function ellipsize(s: string, max: number): string {
  if (s.length <= max) return s;
  return s.slice(0, max) + '…';
}
