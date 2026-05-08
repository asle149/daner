'use client';

import { useState } from 'react';
import type { Comment } from '@/types/api';
import { AuthorLine } from './AuthorLine';
import { ReplyList } from './ReplyList';
import { LikeButton } from './LikeButton';
import { DeleteButton } from './DeleteButton';
import { Composer } from './Composer';
import { timeAgo } from '@/lib/util/timeAgo';

export function CommentItem({ comment, word }: { comment: Comment; word: string }) {
  const [open, setOpen] = useState(false);
  const [composeReply, setComposeReply] = useState(false);

  return (
    <article className="py-3">
      <p className="text-sm">{comment.content}</p>
      <div className="mt-1 flex items-center justify-between">
        <AuthorLine author={comment.author} time={timeAgo(comment.createdAt)} />
        <div className="flex items-center gap-3">
          <button
            type="button"
            onClick={() => {
              setComposeReply((v) => !v);
              if (!open && comment.replyCount > 0) setOpen(true);
            }}
            className="text-[11px] text-tertiary"
          >
            답글
          </button>
          {comment.replyCount > 0 ? (
            <button
              type="button"
              onClick={() => setOpen((v) => !v)}
              className="text-[11px] text-tertiary"
            >
              {open ? '접기' : `+${comment.replyCount}`}
            </button>
          ) : null}
          <LikeButton commentId={comment.id} initialCount={comment.likeCount} initialLiked={comment.isLiked} />
          <DeleteButton commentId={comment.id} author={comment.author} word={word} />
        </div>
      </div>
      {composeReply ? (
        <div className="ml-4 mt-3 border-l border-dashed border-hairline pl-3">
          <Composer
            kind="reply"
            word={word}
            parentId={comment.id}
            onSent={() => {
              setComposeReply(false);
              setOpen(true);
            }}
          />
        </div>
      ) : null}
      {open ? <ReplyList commentId={comment.id} word={word} /> : null}
    </article>
  );
}
