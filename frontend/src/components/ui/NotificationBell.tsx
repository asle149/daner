'use client';

import Link from 'next/link';
import { useQuery } from '@tanstack/react-query';
import { fetchNotifications } from '@/lib/api/endpoints';
import { useAuth } from '@/lib/auth/AuthContext';

export function NotificationBell() {
  const { isAuthenticated } = useAuth();

  const head = useQuery({
    queryKey: ['notifications-unread'],
    queryFn: () => fetchNotifications(),
    enabled: isAuthenticated,
    staleTime: 60_000,
  });

  if (!isAuthenticated) return null;

  const hasUnread = head.data?.notifications.some((n) => !n.isRead) ?? false;

  return (
    <Link href="/me/notifications" className="relative inline-flex items-center" aria-label="알림">
      <span className="text-secondary">◌</span>
      {hasUnread ? (
        <span className="absolute -right-1 -top-1 inline-block h-1.5 w-1.5 rounded-full bg-accent" />
      ) : null}
    </Link>
  );
}
