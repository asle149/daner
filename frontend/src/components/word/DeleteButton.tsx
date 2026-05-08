'use client';

import { useMutation, useQueryClient } from '@tanstack/react-query';
import { deleteComment } from '@/lib/api/endpoints';
import { ApiError } from '@/lib/api/client';
import { useAuth } from '@/lib/auth/AuthContext';
import type { Author } from '@/types/api';

type Props = {
  commentId: number;
  author: Author;
  word: string;
  onDeleted?: () => void;
};

export function DeleteButton({ commentId, author, word, onDeleted }: Props) {
  const { user } = useAuth();
  const queryClient = useQueryClient();

  const isOwn = author.type === 'user' && user?.id === author.id;
  const mutation = useMutation({
    mutationFn: () => deleteComment(commentId),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['comments', word] });
      void queryClient.invalidateQueries({ queryKey: ['word', word] });
      onDeleted?.();
    },
    onError: (err) => {
      const msg = err instanceof ApiError ? err.message : '삭제하지 못했어요.';
      alert(msg);
    },
  });

  if (!isOwn) return null;

  return (
    <button
      type="button"
      className="text-[11px] text-tertiary"
      disabled={mutation.isPending}
      onClick={() => {
        if (window.confirm('지울까요?')) mutation.mutate();
      }}
    >
      지우기
    </button>
  );
}
