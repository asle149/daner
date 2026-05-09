'use client';

import Link from 'next/link';
import type { MyProfile } from '@/lib/api/endpoints';

const MIN_WIDTH = 20;
const MAX_WIDTH = 44;
const BOOK_HEIGHT = 112;

function bookWidth(myCommentCount: number): number {
  // 폭은 글 수에 비례 (제곱근으로 완만하게, 캡 적용)
  const raw = MIN_WIDTH + Math.sqrt(Math.max(0, myCommentCount)) * 5;
  return Math.min(MAX_WIDTH, Math.round(raw));
}

function Book({
  word,
  href,
  myCommentCount,
}: {
  word: string;
  href: string;
  myCommentCount: number;
}) {
  const width = bookWidth(myCommentCount);
  return (
    <Link
      href={href}
      style={{ width: `${width}px`, height: `${BOOK_HEIGHT}px` }}
      className="flex items-end justify-center rounded-t-md border border-hairline-strong bg-hairline/40 text-foreground transition-colors hover:bg-hairline/70"
      title={`${word} · ${myCommentCount}개의 글`}
    >
      <span
        className="px-1 pb-2 font-display text-[13px] tracking-tight"
        style={{ writingMode: 'vertical-rl', textOrientation: 'mixed' }}
      >
        {word}
      </span>
    </Link>
  );
}

export function Bookshelf({ words }: { words: MyProfile['myWords'] }) {
  const PER_SHELF = 10;
  const shelves: MyProfile['myWords'][] = [];
  for (let i = 0; i < words.length; i += PER_SHELF) {
    shelves.push(words.slice(i, i + PER_SHELF));
  }

  if (shelves.length === 0) {
    return (
      <p className="mt-12 text-center text-sm text-tertiary">
        아직 모은 단어가 없어요. 어떤 단어든 한마디를 남겨보세요.
      </p>
    );
  }

  return (
    <div className="mt-10 space-y-7">
      {shelves.map((row, idx) => (
        <div key={idx}>
          <div className="flex items-end gap-1.5 px-1">
            {row.map((w) => (
              <Book
                key={w.id}
                word={w.word}
                myCommentCount={w.myCommentCount}
                href={`/words/${encodeURIComponent(w.word)}`}
              />
            ))}
          </div>
          <div className="border-b border-dashed border-hairline-strong" />
        </div>
      ))}
    </div>
  );
}
