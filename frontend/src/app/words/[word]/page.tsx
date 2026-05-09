'use client';

import { use } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Header } from '@/components/ui/Header';
import { CommentList } from '@/components/word/CommentList';
import { Composer } from '@/components/word/Composer';
import { fetchWordRoom } from '@/lib/api/endpoints';

export default function WordRoomPage({ params }: { params: Promise<{ word: string }> }) {
  const { word: encodedWord } = use(params);
  const word = decodeURIComponent(encodedWord);

  const room = useQuery({
    queryKey: ['word', word],
    queryFn: () => fetchWordRoom(word),
  });

  return (
    <>
      <Header />
      <main className="mx-auto flex w-full max-w-2xl flex-1 flex-col px-6 pb-32">
        <header className="mt-12 text-center">
          <h1 className="font-display text-5xl font-bold tracking-wide">{word}</h1>
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
