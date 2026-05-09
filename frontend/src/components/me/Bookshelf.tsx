'use client';

import Link from 'next/link';
import type { MyProfile } from '@/lib/api/endpoints';

const MIN_WIDTH = 20;
const MAX_RATIO = 5;
const MAX_WIDTH = MIN_WIDTH * MAX_RATIO;
const BOOK_HEIGHT = 112;
const PER_SHELF = 10;

// 책 두께: 1개면 기본, 2개부터 책장 안 max값에 비례해 두꺼워짐, 최대 5배
function bookWidth(myCommentCount: number, maxCount: number): number {
  if (myCommentCount <= 1 || maxCount <= 1) return MIN_WIDTH;
  const ratio = (myCommentCount - 1) / (maxCount - 1);
  return Math.round(MIN_WIDTH + (MAX_WIDTH - MIN_WIDTH) * Math.min(1, ratio));
}

function Book({
  word,
  href,
  width,
  myCommentCount,
}: {
  word: string;
  href: string;
  width: number;
  myCommentCount: number;
}) {
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
  const maxCount = words.reduce((acc, w) => Math.max(acc, w.myCommentCount), 0);

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
          <div className="flex flex-wrap items-end gap-1.5 px-1">
            {row.map((w) => (
              <Book
                key={w.id}
                word={w.word}
                myCommentCount={w.myCommentCount}
                width={bookWidth(w.myCommentCount, maxCount)}
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
