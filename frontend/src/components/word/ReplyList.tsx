'use client';

import { useInfiniteQuery } from '@tanstack/react-query';
import { fetchReplies } from '@/lib/api/endpoints';
import type { Reply } from '@/types/api';
import { timeAgo } from '@/lib/util/timeAgo';
import { AuthorLine } from './AuthorLine';
import { LikeButton } from './LikeButton';
import { DeleteButton } from './DeleteButton';

export function ReplyList({ commentId, word }: { commentId: number; word: string }) {
  const query = useInfiniteQuery({
    queryKey: ['replies', commentId],
    queryFn: ({ pageParam }) => fetchReplies(commentId, pageParam ?? null),
    initialPageParam: null as string | null,
    getNextPageParam: (last) => last.nextCursor,
  });

  const replies: Reply[] = query.data?.pages.flatMap((p) => p.replies) ?? [];

  return (
    <div className="divide-y divide-dashed divide-hairline">
      {replies.map((r) => (
        <article key={r.id} className="space-y-1 py-3 first:pt-0">
          <p className="text-[13px] text-secondary">{r.content}</p>
          <div className="flex items-center justify-between">
            <AuthorLine author={r.author} time={timeAgo(r.createdAt)} />
            <div className="flex items-center gap-3">
              <LikeButton
                commentId={r.id}
                initialCount={r.likeCount}
                initialLiked={r.isLiked}
                word={word}
              />
              <DeleteButton commentId={r.id} isMine={r.isMine} word={word} />
            </div>
          </div>
        </article>
      ))}
      {query.hasNextPage ? (
        <button
          type="button"
          onClick={() => void query.fetchNextPage()}
          className="py-2 text-[11px] text-tertiary"
          disabled={query.isFetchingNextPage}
        >
          {query.isFetchingNextPage ? '불러오는 중…' : '더 보기'}
        </button>
      ) : null}
    </div>
  );
}
