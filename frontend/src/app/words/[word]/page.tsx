'use client';

import { use } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Header } from '@/components/ui/Header';
import { CommentList } from '@/components/word/CommentList';
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
      <Header back />
      <main className="mx-auto flex w-full max-w-2xl flex-1 flex-col px-6 pb-32">
        <header className="mt-12 text-center">
          <h1 className="text-3xl font-medium tracking-wide">{word}</h1>
          {room.data?.exists ? (
            <p className="mt-2 text-sm text-secondary">
              {room.data.commentCount}개의 마음
            </p>
          ) : room.data?.exists === false ? (
            <p className="mt-2 text-sm text-secondary">{room.data.message}</p>
          ) : null}
        </header>

        <CommentList word={word} />
      </main>
    </>
  );
}
