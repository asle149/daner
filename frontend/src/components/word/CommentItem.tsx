'use client';

import { useEffect, useState } from 'react';
import type { Comment } from '@/types/api';
import { AuthorLine } from './AuthorLine';
import { ReplyList } from './ReplyList';
import { LikeButton } from './LikeButton';
import { DeleteButton } from './DeleteButton';
import { Composer } from './Composer';
import { timeAgo } from '@/lib/util/timeAgo';

const REPLIES_OPEN_PREFIX = 'daner.replies-open.';

export function CommentItem({ comment, word }: { comment: Comment; word: string }) {
  const [open, setOpen] = useState(false);
  const [composeReply, setComposeReply] = useState(false);

  // 펼침 상태는 localStorage 에 "펼친 것만" 저장 (접은 건 키 제거).
  // 알림으로 들어오면 hash/?p= 로 자동 펼침은 그대로 유지.
  useEffect(() => {
    if (typeof window === 'undefined') return;
    if (localStorage.getItem(`${REPLIES_OPEN_PREFIX}${comment.id}`) === '1') {
      setOpen(true);
      return;
    }
    const sp = new URLSearchParams(window.location.search);
    if (sp.get('p') === String(comment.id)) {
      setOpen(true);
      return;
    }
    const hash = window.location.hash;
    if (
      hash.startsWith('#comment-') &&
      hash !== `#comment-${comment.id}` &&
      comment.replyCount > 0
    ) {
      setOpen(true);
    }
  }, [comment.id, comment.replyCount]);

  const updateOpen = (next: boolean) => {
    setOpen(next);
    if (typeof window === 'undefined') return;
    const key = `${REPLIES_OPEN_PREFIX}${comment.id}`;
    if (next) localStorage.setItem(key, '1');
    else localStorage.removeItem(key);
  };

  const openReplies = () => {
    updateOpen(true);
    setComposeReply(true);
  };

  return (
    <article id={`comment-${comment.id}`} className="py-3 scroll-mt-24">
      <p className="font-display text-base leading-relaxed">{comment.content}</p>
      <div className="mt-1 flex items-center justify-between">
        <AuthorLine author={comment.author} time={timeAgo(comment.createdAt)} />
        <div className="flex items-center gap-3">
          <button
            type="button"
            onClick={() => (composeReply ? setComposeReply(false) : openReplies())}
            className="font-display text-[13px] text-tertiary"
          >
            답글
          </button>
          {comment.replyCount > 0 ? (
            <button
              type="button"
              onClick={() => updateOpen(!open)}
              className="font-display text-[13px] text-tertiary"
            >
              {open ? '접기' : `+${comment.replyCount}`}
            </button>
          ) : null}
          <LikeButton
            commentId={comment.id}
            initialCount={comment.likeCount}
            initialLiked={comment.isLiked}
            word={word}
          />
          <DeleteButton commentId={comment.id} isMine={comment.isMine} word={word} />
        </div>
      </div>

      {open ? (
        <div className="ml-4 mt-3 border-l border-dashed border-hairline pl-3">
          <ReplyList commentId={comment.id} word={word} />
          {composeReply ? (
            <div className="mt-4">
              <Composer
                kind="reply"
                word={word}
                parentId={comment.id}
                onSent={() => setComposeReply(false)}
              />
            </div>
          ) : null}
        </div>
      ) : composeReply ? (
        <div className="ml-4 mt-3 border-l border-dashed border-hairline pl-3">
          <Composer
            kind="reply"
            word={word}
            parentId={comment.id}
            onSent={() => setComposeReply(false)}
          />
        </div>
      ) : null}
    </article>
  );
}
