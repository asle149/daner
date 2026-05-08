'use client';

import { useState } from 'react';
import { useMutation } from '@tanstack/react-query';
import { likeComment, unlikeComment } from '@/lib/api/endpoints';
import { ApiError } from '@/lib/api/client';
import { useAuth } from '@/lib/auth/AuthContext';

type Props = {
  commentId: number;
  initialCount: number;
  initialLiked: boolean;
};

export function LikeButton({ commentId, initialCount, initialLiked }: Props) {
  const { isAuthenticated } = useAuth();
  const [count, setCount] = useState(initialCount);
  const [liked, setLiked] = useState(initialLiked);

  const toggle = useMutation({
    mutationFn: () => (liked ? unlikeComment(commentId) : likeComment(commentId)),
    onMutate: () => {
      setLiked((v) => !v);
      setCount((c) => c + (liked ? -1 : 1));
    },
    onError: (err) => {
      // rollback
      setLiked((v) => !v);
      setCount((c) => c + (liked ? 1 : -1));
      if (err instanceof ApiError && err.code === 'ALREADY_LIKED') {
        setLiked(true);
      }
    },
    onSuccess: (data) => {
      setCount(data.likeCount);
      setLiked(data.isLiked);
    },
  });

  if (!isAuthenticated) {
    return <span className="text-[11px] text-tertiary">♡ {count}</span>;
  }

  return (
    <button
      type="button"
      onClick={() => toggle.mutate()}
      className="text-[11px] text-tertiary disabled:opacity-50"
      disabled={toggle.isPending}
      aria-pressed={liked}
    >
      <span className={liked ? 'text-accent' : undefined}>♡</span> {count}
    </button>
  );
}
