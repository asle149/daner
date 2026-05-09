'use client';

import { useEffect, useState, type FormEvent } from 'react';
import { useRouter } from 'next/navigation';
import { useQuery } from '@tanstack/react-query';
import { Header } from '@/components/ui/Header';
import { fetchHome } from '@/lib/api/endpoints';
import { normalizeForRouting } from '@/lib/util/normalizeWord';

const VISITED_KEY = 'daner.visited';

export default function HomePage() {
  const router = useRouter();
  const [value, setValue] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [ready, setReady] = useState(false);

  // 첫 방문자는 /welcome 으로 안내. 본 페이지 렌더링은 그 이후.
  useEffect(() => {
    if (typeof window === 'undefined') return;
    if (!localStorage.getItem(VISITED_KEY)) {
      router.replace('/welcome');
      return;
    }
    setReady(true);
  }, [router]);

  const home = useQuery({ queryKey: ['home'], queryFn: fetchHome, enabled: ready });

  const onSubmit = (e: FormEvent) => {
    e.preventDefault();
    setError(null);
    try {
      const word = normalizeForRouting(value);
      if (!word) return;
      router.push(`/words/${encodeURIComponent(word)}`);
    } catch (err) {
      setError(err instanceof Error ? err.message : '잘못된 단어예요.');
    }
  };

  if (!ready) return null;

  return (
    <>
      <Header />
      <main className="flex flex-1 flex-col items-center justify-center px-6 pb-16">
        <div className="w-full max-w-md text-center">
          <p className="text-xl font-bold text-secondary">오늘의 단어는?</p>
          <form onSubmit={onSubmit} className="mt-10">
            <input
              autoFocus
              className="input-underline text-center text-2xl"
              value={value}
              onChange={(e) => setValue(e.target.value)}
              maxLength={20}
            />
          </form>
          <p className="mt-2 min-h-5 text-sm text-accent">{error}</p>

          {(home.data?.myWords.length ?? 0) > 0 ? (
            <div className="mt-12 flex items-center justify-center gap-5 text-base text-tertiary/80">
              {home.data!.myWords.slice(0, 3).map((w) => (
                <a
                  key={w.id}
                  href={`/words/${encodeURIComponent(w.word)}`}
                  className="hover:text-secondary"
                >
                  {w.word}
                </a>
              ))}
            </div>
          ) : null}
        </div>

        {home.data?.popularWords.length ? (
          <div className="mt-auto pt-16 w-full max-w-md text-center">
            <p className="text-xs tracking-widest text-tertiary">지금 모이는</p>
            <div className="mt-3 flex items-center justify-center gap-6 text-base text-secondary">
              {home.data.popularWords.map((w) => (
                <a
                  key={w.id}
                  href={`/words/${encodeURIComponent(w.word)}`}
                  className="hover:text-foreground"
                >
                  {w.word}
                </a>
              ))}
            </div>
          </div>
        ) : null}
      </main>
    </>
  );
}
