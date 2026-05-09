'use client';

import { useEffect } from 'react';

type Props = {
  open: boolean;
  onClose: () => void;
};

const FEEDBACK_EMAIL = 'asle14910@gmail.com';

export function HelpDialog({ open, onClose }: Props) {
  useEffect(() => {
    if (!open) return;
    const handler = (e: KeyboardEvent) => {
      if (e.key === 'Escape') onClose();
    };
    document.addEventListener('keydown', handler);
    return () => document.removeEventListener('keydown', handler);
  }, [open, onClose]);

  if (!open) return null;

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-foreground/30 px-6"
      onClick={onClose}
    >
      <div
        className="w-full max-w-md space-y-5 rounded bg-background p-8 text-sm"
        onClick={(e) => e.stopPropagation()}
      >
        <h2 className="text-center text-2xl tracking-[0.2em]">DANER</h2>
        <p className="leading-relaxed text-secondary">
          하나의 단어가 하나의 방이 됩니다. 같은 단어를 떠올린 사람들이 서로 이야기를 주고받는
          곳이에요. 혹은 친구끼리 우리만의 단어방을 만들 수도 있죠.
        </p>
        <p className="leading-relaxed text-secondary">
          이미 누군가 머물렀던 단어라면 그 안에 남겨진 생각들을 만날 수 있습니다. 아직 아무도
          찾지 않은 단어라면 당신의 한 마디로 새로운 방이 열려요.
        </p>
        <div className="border-t border-dashed border-hairline pt-4 text-tertiary">
          <p className="mb-1 text-[11px] tracking-widest">의견·문의</p>
          <a
            href={`mailto:${FEEDBACK_EMAIL}`}
            className="text-foreground hover:text-accent"
          >
            {FEEDBACK_EMAIL}
          </a>
        </div>
        <div className="text-right">
          <button type="button" onClick={onClose} className="text-tertiary">
            닫기
          </button>
        </div>
      </div>
    </div>
  );
}
