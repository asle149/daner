'use client';

import { useState, type FormEvent } from 'react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { createComment, createReply } from '@/lib/api/endpoints';
import { ApiError } from '@/lib/api/client';
import { useAuth } from '@/lib/auth/AuthContext';

type Props =
  | { kind: 'comment'; word: string; parentId?: undefined; onSent?: () => void }
  | { kind: 'reply'; word: string; parentId: number; onSent?: () => void };

export function Composer(props: Props) {
  const { isAuthenticated } = useAuth();
  const queryClient = useQueryClient();
  const [content, setContent] = useState('');
  const [anonymous, setAnonymous] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const mutation = useMutation<unknown, Error>({
    mutationFn: async () => {
      if (props.kind === 'comment') {
        return createComment(props.word, { content, anonymous }, !isAuthenticated);
      }
      return createReply(props.parentId, { content, anonymous }, !isAuthenticated);
    },
    onSuccess: () => {
      setContent('');
      if (props.kind === 'comment') {
        void queryClient.invalidateQueries({ queryKey: ['comments', props.word] });
        void queryClient.invalidateQueries({ queryKey: ['word', props.word] });
      } else {
        void queryClient.invalidateQueries({ queryKey: ['replies', props.parentId] });
        void queryClient.invalidateQueries({ queryKey: ['comments', props.word] });
      }
      props.onSent?.();
    },
    onError: (err) => {
      setError(err instanceof ApiError ? err.message : '잠시 후 다시 시도해주세요.');
    },
  });

  const placeholder = props.kind === 'comment'
    ? '이 단어에 대해 한마디'
    : '답글을 남겨보세요';

  const onSubmit = (e: FormEvent) => {
    e.preventDefault();
    setError(null);
    if (!content.trim()) return;
    mutation.mutate();
  };

  return (
    <form onSubmit={onSubmit} className="border-t border-dashed border-hairline pt-3">
      <div className="flex items-center gap-2">
        <input
          className="input-underline flex-1 text-sm"
          placeholder={placeholder}
          value={content}
          onChange={(e) => setContent(e.target.value)}
          maxLength={1000}
          disabled={mutation.isPending}
        />
        <button
          type="submit"
          className="text-sm text-secondary disabled:text-tertiary"
          disabled={mutation.isPending || !content.trim()}
        >
          {mutation.isPending ? '...' : '쓰기'}
        </button>
      </div>
      <div className="mt-2 flex items-center justify-between text-[11px] text-tertiary">
        {isAuthenticated ? (
          <label className="flex items-center gap-1">
            <input
              type="checkbox"
              checked={anonymous}
              onChange={(e) => setAnonymous(e.target.checked)}
              className="h-3 w-3"
            />
            익명으로
          </label>
        ) : (
          <span>비회원으로 한마디</span>
        )}
        {error ? <span className="text-accent">{error}</span> : null}
      </div>
    </form>
  );
}
