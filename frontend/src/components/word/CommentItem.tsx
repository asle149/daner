'use client';

import { useState } from 'react';
import type { Comment } from '@/types/api';
import { AuthorLine } from './AuthorLine';
import { ReplyList } from './ReplyList';
import { timeAgo } from '@/lib/util/timeAgo';

export function CommentItem({ comment }: { comment: Comment }) {
  const [open, setOpen] = useState(false);

  return (
    <article className="py-3">
      <p className="text-sm">{comment.content}</p>
      <div className="mt-1 flex items-center justify-between text-tertiary">
        <AuthorLine author={comment.author} time={timeAgo(comment.createdAt)} />
        <div className="flex items-center gap-3 text-[11px]">
          {comment.replyCount > 0 ? (
            <button type="button" onClick={() => setOpen((v) => !v)} className="text-tertiary">
              {open ? '답글 닫기' : `답글 ${comment.replyCount}`}
            </button>
          ) : null}
          <span>♡ {comment.likeCount}</span>
        </div>
      </div>
      {open ? <ReplyList commentId={comment.id} /> : null}
    </article>
  );
}
