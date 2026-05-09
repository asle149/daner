'use client';

import Link from 'next/link';
import { useState } from 'react';
import { useAuth } from '@/lib/auth/AuthContext';
import { NotificationBell } from './NotificationBell';
import { HelpDialog } from './HelpDialog';

const OAUTH_START =
  process.env.NEXT_PUBLIC_OAUTH_START_URL ?? 'http://localhost:8080/v1/auth/google';

export function Header() {
  const { isAuthenticated, loading } = useAuth();
  const [helpOpen, setHelpOpen] = useState(false);

  return (
    <>
      <header className="flex items-center justify-between px-6 py-4 text-sm">
        <button
          type="button"
          onClick={() => setHelpOpen(true)}
          aria-label="도움말"
          className="text-tertiary hover:text-secondary"
        >
          ?
        </button>

        <Link href="/" className="font-display text-lg tracking-[0.2em] text-foreground">
          DANER
        </Link>

        <div className="flex items-center gap-3">
          {loading ? null : isAuthenticated ? (
            <>
              <NotificationBell />
              <Link
                href="/me"
                aria-label="내 책장"
                className="text-secondary hover:text-foreground"
              >
                <BookshelfIcon />
              </Link>
            </>
          ) : (
            <a href={OAUTH_START} className="text-secondary">
              로그인
            </a>
          )}
        </div>
      </header>
      <HelpDialog open={helpOpen} onClose={() => setHelpOpen(false)} />
    </>
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
