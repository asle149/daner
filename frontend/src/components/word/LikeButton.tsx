'use client';

import { useEffect, useState } from 'react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { likeComment, unlikeComment } from '@/lib/api/endpoints';
import { ApiError } from '@/lib/api/client';
import { useAuth } from '@/lib/auth/AuthContext';

type Props = {
  commentId: number;
  initialCount: number;
  initialLiked: boolean;
  word: string;
};

export function LikeButton({ commentId, initialCount, initialLiked, word }: Props) {
  const { isAuthenticated } = useAuth();
  const queryClient = useQueryClient();
  const [count, setCount] = useState(initialCount);
  const [liked, setLiked] = useState(initialLiked);

  // 부모가 새 데이터로 다시 렌더되면 동기화 (정렬 토글, 댓글 invalidate 후 등)
  useEffect(() => {
    setCount(initialCount);
    setLiked(initialLiked);
  }, [initialCount, initialLiked]);

  const toggle = useMutation({
    mutationFn: (currentlyLiked: boolean) =>
      currentlyLiked ? unlikeComment(commentId) : likeComment(commentId),
    onSuccess: (data) => {
      setCount(data.likeCount);
      setLiked(data.isLiked);
      void queryClient.invalidateQueries({ queryKey: ['comments', word] });
      void queryClient.invalidateQueries({ queryKey: ['replies'] });
    },
    onError: (err) => {
      if (err instanceof ApiError && err.code === 'ALREADY_LIKED') {
        setLiked(true);
      }
      void queryClient.invalidateQueries({ queryKey: ['comments', word] });
    },
  });

  if (!isAuthenticated) {
    return <span className="font-display text-[12px] text-tertiary">♡ {count}</span>;
  }

  const onClick = () => {
    if (toggle.isPending) return;
    toggle.mutate(liked);
  };

  return (
    <button
      type="button"
      onClick={onClick}
      className="font-display text-[12px] disabled:opacity-50"
      disabled={toggle.isPending}
      aria-pressed={liked}
    >
      <span className={liked ? 'text-accent' : 'text-tertiary'}>
        {liked ? '♥' : '♡'}
      </span>{' '}
      <span className="text-tertiary">{count}</span>
    </button>
  );
}
