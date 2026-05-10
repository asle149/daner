'use client';

import { use, useEffect } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Header } from '@/components/ui/Header';
import { CommentList } from '@/components/word/CommentList';
import { Composer } from '@/components/word/Composer';
import { fetchWordRoom } from '@/lib/api/endpoints';
import { chunkWord } from '@/lib/util/chunkWord';

export default function WordRoomPage({ params }: { params: Promise<{ word: string }> }) {
  const { word: encodedWord } = use(params);
  const word = decodeURIComponent(encodedWord);

  const room = useQuery({
    queryKey: ['word', word],
    queryFn: () => fetchWordRoom(word),
  });

  // 알림 등에서 #comment-123 해시로 들어오면 댓글 로드 후 그 댓글로 스크롤 + 잠깐 하이라이트
  useEffect(() => {
    if (typeof window === 'undefined') return;
    const hash = window.location.hash;
    if (!hash || !hash.startsWith('#comment-')) return;
    if (room.isLoading) return;
    const id = hash.slice(1);
    let attempts = 0;
    const tick = () => {
      const el = document.getElementById(id);
      if (el) {
        el.scrollIntoView({ behavior: 'smooth', block: 'center' });
        el.classList.add('comment-flash');
        window.setTimeout(() => el.classList.remove('comment-flash'), 1600);
        return;
      }
      if (attempts++ < 15) {
        window.setTimeout(tick, 200);
      }
    };
    tick();
  }, [room.isLoading]);

  return (
    <>
      <Header />
      <main className="mx-auto flex w-full max-w-2xl flex-1 flex-col px-6 pb-32">
        <header className="mt-12 text-center">
          <h1 className="font-display text-4xl font-bold leading-tight -tracking-[0.01em]">
            {chunkWord(word).map((chunk, i) => (
              <span key={i} className="block">
                {chunk}
              </span>
            ))}
          </h1>
          {room.data?.exists ? (
            <p className="mt-2 font-display text-sm text-secondary">
              {room.data.commentCount}개의 이야기
            </p>
          ) : room.data?.exists === false ? (
            <p className="mt-2 font-display text-sm text-secondary">{room.data.message}</p>
          ) : null}
        </header>

        <CommentList word={word} />
      </main>

      <div className="fixed inset-x-0 bottom-0 border-t border-hairline bg-background/95 px-6 py-3 backdrop-blur">
        <div className="mx-auto max-w-2xl">
          <Composer kind="comment" word={word} />
        </div>
      </div>
    </>
  );
}
