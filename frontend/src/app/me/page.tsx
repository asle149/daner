'use client';

import { useEffect, useMemo, useState } from 'react';
import { useRouter } from 'next/navigation';
import { useInfiniteQuery } from '@tanstack/react-query';
import { Header } from '@/components/ui/Header';
import { Bookshelf } from '@/components/me/Bookshelf';
import { SkeletonBookshelf } from '@/components/ui/Skeleton';
import { fetchMyProfile } from '@/lib/api/endpoints';
import { useAuth } from '@/lib/auth/AuthContext';

type SortMode = 'recent' | 'count';

export default function MyProfilePage() {
  const router = useRouter();
  const { user, loading, isAuthenticated, logout } = useAuth();
  const [sort, setSort] = useState<SortMode>('recent');

  useEffect(() => {
    if (!loading && !isAuthenticated) router.replace('/');
  }, [loading, isAuthenticated, router]);

  const profile = useInfiniteQuery({
    queryKey: ['me-profile'],
    queryFn: ({ pageParam }) => fetchMyProfile(pageParam ?? null),
    initialPageParam: null as string | null,
    getNextPageParam: (last) => last.nextCursor,
    enabled: isAuthenticated,
  });

  const allWords = profile.data?.pages.flatMap((p) => p.myWords) ?? [];
  const sortedWords = useMemo(() => {
    const arr = [...allWords];
    if (sort === 'count') {
      arr.sort((a, b) => b.myCommentCount - a.myCommentCount);
    } else {
      arr.sort((a, b) => b.lastActivityAt.localeCompare(a.lastActivityAt));
    }
    return arr;
  }, [allWords, sort]);

  const onLogout = async () => {
    await logout();
    router.replace('/');
  };

  return (
    <>
      <Header />
      <main className="mx-auto flex w-full max-w-2xl flex-1 flex-col px-6 pb-16">
        <header className="mt-12 text-center">
          <h1 className="font-display text-3xl font-bold">{user?.nickname ?? ''}</h1>
          <p className="mt-2 font-display text-sm text-secondary">
            {allWords.length}개의 단어를 모았어요
          </p>
        </header>

        {allWords.length > 0 ? (
          <div className="mt-8 text-center font-display text-xs">
            <button
              type="button"
              onClick={() => setSort('recent')}
              className={sort === 'recent' ? 'text-foreground' : 'text-tertiary hover:text-secondary'}
            >
              최신순
            </button>
            <span className="mx-2 text-tertiary">·</span>
            <button
              type="button"
              onClick={() => setSort('count')}
              className={sort === 'count' ? 'text-foreground' : 'text-tertiary hover:text-secondary'}
            >
              많이 남긴 순
            </button>
          </div>
        ) : null}

        {profile.isLoading ? <SkeletonBookshelf /> : <Bookshelf words={sortedWords} /> }

        {profile.hasNextPage ? (
          <button
            type="button"
            onClick={() => void profile.fetchNextPage()}
            className="mt-6 w-full py-2 font-display text-xs text-tertiary"
            disabled={profile.isFetchingNextPage}
          >
            {profile.isFetchingNextPage ? '불러오는 중…' : '더 보기'}
          </button>
        ) : null}

        <div className="mt-12 text-center">
          <button
            type="button"
            onClick={onLogout}
            className="font-display text-[12px] tracking-widest text-tertiary hover:text-secondary"
          >
            로그아웃
          </button>
        </div>
      </main>
    </>
  );
}
