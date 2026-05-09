'use client';

import Link from 'next/link';
import { useAuth } from '@/lib/auth/AuthContext';
import { NotificationBell } from './NotificationBell';

const OAUTH_START =
  process.env.NEXT_PUBLIC_OAUTH_START_URL ?? 'http://localhost:8080/v1/auth/google';

export function Header() {
  const { isAuthenticated, loading } = useAuth();

  return (
    <header className="flex items-center justify-between px-6 py-4 text-sm">
      <div className="flex w-20 items-center justify-start">
        {loading ? null : isAuthenticated ? <NotificationBell /> : null}
      </div>

      <Link
        href="/"
        className="font-display text-lg font-bold tracking-[0.2em] text-foreground"
      >
        DANER
      </Link>

      <div className="flex w-20 items-center justify-end gap-3">
        {loading ? null : isAuthenticated ? (
          <Link
            href="/me"
            aria-label="내 책장"
            className="text-secondary hover:text-foreground"
          >
            <BookshelfIcon />
          </Link>
        ) : (
          <a href={OAUTH_START} className="text-secondary">
            로그인
          </a>
        )}
      </div>
    </header>
  );
}

function BookshelfIcon() {
  return (
    <svg
      width="18"
      height="18"
      viewBox="0 0 18 18"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.2"
      aria-hidden
    >
      <rect x="2" y="3" width="2.5" height="11" />
      <rect x="5" y="2" width="2.5" height="12" />
      <rect x="8" y="4" width="2.5" height="10" />
      <rect x="11" y="3.5" width="2.5" height="10.5" />
      <line x1="1.5" y1="14.5" x2="16.5" y2="14.5" strokeDasharray="1 1.5" />
    </svg>
  );
}
