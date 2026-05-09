'use client';

import { useRouter } from 'next/navigation';
import Link from 'next/link';

const VISITED_KEY = 'daner.visited';

export default function WelcomePage() {
  const router = useRouter();

  const start = () => {
    if (typeof window !== 'undefined') {
      localStorage.setItem(VISITED_KEY, '1');
    }
    router.replace('/');
  };

  return (
    <main className="flex flex-1 flex-col px-6 py-10">
      <div className="mx-auto w-full max-w-lg space-y-8">
        <Link
          href="/"
          className="block text-center text-lg font-bold tracking-[0.2em] text-foreground"
        >
          DANER
        </Link>

        <div className="space-y-6 text-[15px] leading-relaxed">
          <p>
            <strong className="font-bold underline underline-offset-4">단어를 검색해보세요.</strong>{' '}
            <span className="text-secondary">홈에서 떠오르는 단어를 입력합니다.</span>
          </p>
          <p>
            <strong className="font-bold underline underline-offset-4">
              누군가 먼저 도착했다면 그 방에 들어갑니다.
            </strong>{' '}
            <span className="text-secondary">
              다른 사람들의 한마디가 보여요. 좋아요를 누르거나, 답글을 달거나, 자신의 한마디를
              남길 수 있어요.
            </span>
          </p>
          <p>
            <strong className="font-bold underline underline-offset-4">
              처음 도착한 단어라면 첫 한마디를 남겨보세요.
            </strong>{' '}
            <span className="text-secondary">그 순간 그 단어의 방이 만들어집니다.</span>
          </p>
          <p>
            <strong className="font-bold underline underline-offset-4">
              친구끼리만의 단어도 만들 수 있어요.
            </strong>{' '}
            <span className="text-secondary">
              약속한 단어 하나만 공유하면, 그 친구들만의 공간이 됩니다.
            </span>
          </p>
        </div>

        <div className="pt-4 text-center">
          <button
            type="button"
            onClick={start}
            className="border-b border-foreground pb-1 text-base hover:text-accent"
          >
            시작하기!
          </button>
        </div>
      </div>
    </main>
  );
}
