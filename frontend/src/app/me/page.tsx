'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useInfiniteQuery } from '@tanstack/react-query';
import { Header } from '@/components/ui/Header';
import { Bookshelf } from '@/components/me/Bookshelf';
import { SkeletonBookshelf } from '@/components/ui/Skeleton';
import { fetchMyProfile } from '@/lib/api/endpoints';
import { useAuth } from '@/lib/auth/AuthContext';

export default function MyProfilePage() {
  const router = useRouter();
  const { user, loading, isAuthenticated } = useAuth();

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
  const top3 = [...allWords]
    .sort((a, b) => b.myCommentCount - a.myCommentCount)
    .slice(0, 3);

  return (
    <>
      <Header back />
      <main className="mx-auto flex w-full max-w-2xl flex-1 flex-col px-6 pb-16">
        <header className="mt-12 text-center">
          <h1 className="text-[22px] font-medium">{user?.nickname ?? ''}</h1>
          <p className="mt-2 text-sm text-secondary">
            {allWords.length}개의 단어를 모았어요
          </p>
          {top3.length > 0 ? (
            <p className="mt-4 text-[11px] tracking-widest text-tertiary">
              자주 가는 단어 · {top3.map((w) => w.word).join(' · ')}
            </p>
          ) : null}
        </header>

        {profile.isLoading ? <SkeletonBookshelf /> : <Bookshelf words={allWords} />}

        {profile.hasNextPage ? (
          <button
            type="button"
            onClick={() => void profile.fetchNextPage()}
            className="mt-6 w-full py-2 text-xs text-tertiary"
            disabled={profile.isFetchingNextPage}
          >
            {profile.isFetchingNextPage ? '불러오는 중…' : '더 보기'}
          </button>
        ) : null}
      </main>
    </>
  );
}
