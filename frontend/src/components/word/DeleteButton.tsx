'use client';

import { useMutation, useQueryClient } from '@tanstack/react-query';
import { deleteComment } from '@/lib/api/endpoints';
import { ApiError } from '@/lib/api/client';
import { useAuth } from '@/lib/auth/AuthContext';

type Props = {
  commentId: number;
  isMine: boolean;
  word: string;
};

export function DeleteButton({ commentId, isMine, word }: Props) {
  const queryClient = useQueryClient();
  const { isAdmin } = useAuth();

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

  // 본인 또는 관리자만 노출. 관리자에겐 "지우기 (관리자)" 라벨로 구분 표기.
  if (!isMine && !isAdmin) return null;
  const label = !isMine && isAdmin ? '지우기 (관리자)' : '지우기';

  return (
    <button
      type="button"
      className="font-display text-[13px] text-tertiary hover:text-accent"
      disabled={mutation.isPending}
      onClick={() => {
        const msg = !isMine && isAdmin
          ? '관리자 권한으로 이 글을 지웁니다. 계속할까요?'
          : '지울까요?';
        if (window.confirm(msg)) mutation.mutate();
      }}
    >
      {label}
    </button>
  );
}
