'use client';

import Link from 'next/link';
import type { MyProfile } from '@/lib/api/endpoints';

// 책 두께/높이를 단어 길이에 따라 살짝 다르게
function bookGeometry(word: string, index: number): { width: number; height: number } {
  const len = [...word].length;
  const baseHeight = 96 + ((len * 7 + index * 11) % 32);
  const baseWidth = 18 + ((len + index) % 6) * 3;
  return { width: baseWidth, height: baseHeight };
}

function Book({ word, href }: { word: string; href: string }) {
  const { width, height } = bookGeometry(word, word.charCodeAt(0));
  return (
    <Link
      href={href}
      style={{ width: `${width}px`, height: `${height}px` }}
      className="flex items-end justify-center border border-hairline-strong text-foreground"
    >
      <span
        className="px-1 text-[13px] tracking-tight"
        style={{ writingMode: 'vertical-rl', textOrientation: 'mixed' }}
      >
        {word}
      </span>
    </Link>
  );
}

export function Bookshelf({ words }: { words: MyProfile['myWords'] }) {
  // 한 선반에 N권씩 배치
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
    <div className="mt-10 space-y-6">
      {shelves.map((row, idx) => (
        <div key={idx}>
          <div className="flex items-end gap-1 px-1">
            {row.map((w) => (
              <Book key={w.id} word={w.word} href={`/words/${encodeURIComponent(w.word)}`} />
            ))}
          </div>
          <div className="border-b border-dashed border-hairline-strong" />
        </div>
      ))}
    </div>
  );
}
