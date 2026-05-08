import type { Author } from '@/types/api';

export function AuthorLine({ author, time }: { author: Author; time: string }) {
  const name = author.type === 'user' ? author.nickname : author.label;
  return (
    <p className="text-[11px] text-tertiary">
      {name} · {time}
    </p>
  );
}
