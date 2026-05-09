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
  const [showWelcome, setShowWelcome] = useState(false);

  useEffect(() => {
    if (typeof window === 'undefined') return;
    if (!localStorage.getItem(VISITED_KEY)) {
      setShowWelcome(true);
    }
  }, []);

  const dismissWelcome = () => {
    localStorage.setItem(VISITED_KEY, '1');
    setShowWelcome(false);
  };

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
        <div className="mt-24 w-full max-w-md text-center">
          {showWelcome ? <WelcomeIntro onDismiss={dismissWelcome} /> : null}

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

          {(home.data?.myWords.length ?? 0) > 0 ? (
            <div className="mt-12">
              <div className="flex items-center justify-center gap-5 text-base text-secondary">
                {home.data!.myWords.slice(0, 3).map((w) => (
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
        </div>

        <div className="mt-auto mb-12 w-full max-w-md text-center">
          {home.data?.popularWords.length ? (
            <>
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
            </>
          ) : null}
        </div>
      </main>
    </>
  );
}

function WelcomeIntro({ onDismiss }: { onDismiss: () => void }) {
  return (
    <section className="mb-16 space-y-4 text-left text-[13px] leading-relaxed text-secondary">
      <p className="text-center text-base text-foreground">
        단어 하나로 시작되는 작은 방
      </p>
      <p>하나의 단어가 하나의 방이 됩니다. 떠오른 단어를 입력해 보세요.</p>
      <p>
        이미 누군가 머물렀던 단어라면 그 안에 남겨진 생각들을 만날 수 있고, 아직 아무도 찾지
        않은 단어라면 당신의 첫 글로 새로운 방이 열려요.
      </p>
      <p>친구끼리 약속한 단어로 조용히 모일 수도 있어요.</p>
      <div className="text-right">
        <button type="button" onClick={onDismiss} className="text-[11px] text-tertiary">
          닫기
        </button>
      </div>
    </section>
  );
}
