'use client';

import { useEffect, useState } from 'react';
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

  // 알림에서 들어온 경우 답글 목록 자동 펼침.
  // - 새 알림: ?p={parentCommentId} 가 붙음 → 정확히 매칭되는 부모만 펼침
  // - 옛 알림(parentCommentId 없음): hash가 #comment-N 이고 그게 내가 아니면,
  //   답글이 있는 댓글은 일단 펼쳐 둠 (그 안에 타깃이 있을 수 있음)
  useEffect(() => {
    if (typeof window === 'undefined') return;
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

  const openReplies = () => {
    setOpen(true);
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
              onClick={() => setOpen((v) => !v)}
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
