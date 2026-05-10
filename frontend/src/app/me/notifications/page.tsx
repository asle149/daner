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

  // 답글은 개별 알림, 좋아요는 같은 댓글 + 연속된 것만 합쳐서 한 줄로.
  // (사이에 다른 단어/댓글 알림이 끼어 있으면 시간 순서가 흐트러지므로 별개로 둠)
  const buckets: Bucket[] = useMemo(() => {
    const out: Bucket[] = [];
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
      const last = out[out.length - 1];
      if (last && last.kind === 'like' && last.commentId === n.commentId) {
        last.ids.push(n.id);
        last.count += 1;
        if (n.createdAt > last.rep.createdAt) last.rep = n;
        continue;
      }
      out.push({
        kind: 'like',
        key: `l-${n.id}`,
        word: n.word,
        commentId: n.commentId,
        rep: n,
        ids: [n.id],
        count: 1,
      });
    }
    return out;
  }, [notifications]);

  // 단어로 그룹 — 연속된 같은 단어만 묶어서 시간 순서를 보존.
  // (a b a 로 도착한 알림은 a / b / a 세 묶음 그대로 보여 줌)
  const groups = useMemo(() => {
    const out: Array<[string, Bucket[]]> = [];
    for (const b of buckets) {
      const last = out[out.length - 1];
      if (last && last[0] === b.word) {
        last[1].push(b);
      } else {
        out.push([b.word, [b]]);
      }
    }
    return out;
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
            {groups.map(([word, items], gi) => (
              <section key={`${word}-${gi}-${items[0]?.key ?? ''}`}>
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
