'use client';

import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { useAuth } from '@/lib/auth/AuthContext';
import { NotificationBell } from './NotificationBell';

const OAUTH_START =
  process.env.NEXT_PUBLIC_OAUTH_START_URL ?? 'http://localhost:8080/v1/auth/google';

type HeaderProps = {
  back?: boolean;
};

export function Header({ back = false }: HeaderProps) {
  const router = useRouter();
  const { isAuthenticated, loading, logout } = useAuth();

  return (
    <header className="flex items-center justify-between px-6 py-4 text-sm">
      <div className="w-16">
        {back ? (
          <button
            type="button"
            onClick={() => router.back()}
            className="text-secondary"
            aria-label="뒤로"
          >
            ←
          </button>
        ) : null}
      </div>
      <Link href="/" className="text-secondary tracking-wider">
        daner
      </Link>
      <div className="flex w-24 justify-end">
        {loading ? null : isAuthenticated ? (
          <div className="flex items-center gap-3 text-secondary">
            <NotificationBell />
            <Link href="/me" aria-label="프로필">
              내 책장
            </Link>
            <button type="button" onClick={() => void logout()} className="text-tertiary">
              로그아웃
            </button>
          </div>
        ) : (
          <a href={OAUTH_START} className="text-secondary">
            로그인
          </a>
        )}
      </div>
    </header>
  );
}
