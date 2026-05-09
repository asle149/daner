'use client';

import { useState } from 'react';
import { HelpDialog } from './HelpDialog';

export function HelpFloatingButton() {
  const [open, setOpen] = useState(false);
  return (
    <>
      <button
        type="button"
        onClick={() => setOpen(true)}
        aria-label="도움말"
        className="fixed bottom-20 right-6 z-40 flex h-9 w-9 items-center justify-center rounded-full border border-hairline-strong bg-background text-secondary shadow-sm hover:text-foreground"
      >
        ?
      </button>
      <HelpDialog open={open} onClose={() => setOpen(false)} />
    </>
  );
}
