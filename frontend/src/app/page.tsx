'use client';

import { useState, type FormEvent } from 'react';
import { useRouter } from 'next/navigation';
import { useQuery } from '@tanstack/react-query';
import { Header } from '@/components/ui/Header';
import { fetchHome } from '@/lib/api/endpoints';
import { normalizeForRouting } from '@/lib/util/normalizeWord';

export default function HomePage() {
  const router = useRouter();
  const [value, setValue] = useState('');
  const [error, setError] = useState<string | null>(null);

  const home = useQuery({ queryKey: ['home'], queryFn: fetchHome });

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

  return (
    <>
      <Header />
      <main className="flex flex-1 flex-col items-center px-6">
        <div className="mt-32 w-full max-w-md text-center">
          <p className="text-base text-secondary">오늘의 단어는?</p>
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
        </div>

        <div className="mt-auto mb-12 w-full max-w-md text-center">
          <p className="text-xs tracking-widest text-tertiary">지금 모이는</p>
          <div className="mt-3 flex items-center justify-center gap-6 text-base text-secondary">
            {home.data?.popularWords.length ? (
              home.data.popularWords.map((w) => (
                <a
                  key={w.id}
                  href={`/words/${encodeURIComponent(w.word)}`}
                  className="hover:text-foreground"
                >
                  {w.word}
                </a>
              ))
            ) : home.isLoading ? null : (
              <span className="text-tertiary">아직 모인 단어가 없어요</span>
            )}
          </div>
        </div>
      </main>
    </>
  );
}
