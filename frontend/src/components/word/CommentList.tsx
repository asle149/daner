'use client';

import { useState } from 'react';
import { useInfiniteQuery } from '@tanstack/react-query';
import { fetchComments } from '@/lib/api/endpoints';
import type { Comment } from '@/types/api';
import { CommentItem } from './CommentItem';
import { SkeletonComment } from '@/components/ui/Skeleton';

type Sort = 'latest' | 'popular';

export function CommentList({ word }: { word: string }) {
  const [sort, setSort] = useState<Sort>('latest');

  const query = useInfiniteQuery({
    queryKey: ['comments', word, sort],
    queryFn: ({ pageParam }) => fetchComments(word, sort, pageParam ?? null),
    initialPageParam: null as string | null,
    getNextPageParam: (last) => last.nextCursor,
  });

  const comments: Comment[] = query.data?.pages.flatMap((p) => p.comments) ?? [];

  return (
    <section className="mt-6">
      <div className="flex justify-end gap-3 text-xs">
        <button
          type="button"
          className={sort === 'latest' ? 'text-foreground' : 'text-tertiary'}
          onClick={() => setSort('latest')}
        >
          최신
        </button>
        <button
          type="button"
          className={sort === 'popular' ? 'text-foreground' : 'text-tertiary'}
          onClick={() => setSort('popular')}
        >
          인기
        </button>
      </div>
      <div className="mt-3 divide-y divide-hairline divide-dashed">
        {query.isLoading ? (
          <>
            <SkeletonComment />
            <SkeletonComment />
            <SkeletonComment />
          </>
        ) : comments.length === 0 ? (
          <p className="py-6 text-center text-sm text-tertiary">아직 아무도 도착하지 않았어요</p>
        ) : (
          comments.map((c) => <CommentItem key={c.id} comment={c} word={word} />)
        )}
      </div>
      {query.hasNextPage ? (
        <button
          type="button"
          onClick={() => void query.fetchNextPage()}
          className="mt-4 w-full py-2 text-xs text-tertiary"
          disabled={query.isFetchingNextPage}
        >
          {query.isFetchingNextPage ? '불러오는 중…' : '더 보기'}
        </button>
      ) : null}
    </section>
  );
}
