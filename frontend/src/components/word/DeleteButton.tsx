'use client';

import { useMutation, useQueryClient } from '@tanstack/react-query';
import { deleteComment } from '@/lib/api/endpoints';
import { ApiError } from '@/lib/api/client';

type Props = {
  commentId: number;
  isMine: boolean;
  word: string;
};

export function DeleteButton({ commentId, isMine, word }: Props) {
  const queryClient = useQueryClient();

  const mutation = useMutation({
    mutationFn: () => deleteComment(commentId),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['comments', word] });
      void queryClient.invalidateQueries({ queryKey: ['word', word] });
      void queryClient.invalidateQueries({ queryKey: ['replies'] });
    },
    onError: (err) => {
      const msg = err instanceof ApiError ? err.message : '지우지 못했어요.';
      window.alert(msg);
    },
  });

  if (!isMine) return null;

  return (
    <button
      type="button"
      className="text-[11px] text-tertiary hover:text-accent"
      disabled={mutation.isPending}
      onClick={() => {
        if (window.confirm('지울까요?')) mutation.mutate();
      }}
    >
      지우기
    </button>
  );
}
