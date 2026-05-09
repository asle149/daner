'use client';

import { useState } from 'react';
import { usePathname } from 'next/navigation';
import { HelpDialog } from './HelpDialog';

export function HelpFloatingButton() {
  const [open, setOpen] = useState(false);
  const pathname = usePathname();
  const aboveComposer = pathname?.startsWith('/words/');

  return (
    <>
      <button
        type="button"
        onClick={() => setOpen(true)}
        aria-label="도움말"
        className={`fixed right-6 z-40 flex h-9 w-9 items-center justify-center rounded-full border border-hairline-strong bg-background text-secondary shadow-sm hover:text-foreground ${
          aboveComposer ? 'bottom-[100px]' : 'bottom-6'
        }`}
      >
        ?
      </button>
      <HelpDialog open={open} onClose={() => setOpen(false)} />
    </>
  );
}
