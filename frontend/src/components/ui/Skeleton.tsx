export function SkeletonLine({ className = '' }: { className?: string }) {
  return <div className={`h-3 animate-pulse rounded bg-hairline ${className}`} />;
}

export function SkeletonComment() {
  return (
    <div className="space-y-2 py-3">
      <SkeletonLine className="w-3/4" />
      <SkeletonLine className="w-1/2" />
      <SkeletonLine className="w-1/4" />
    </div>
  );
}

export function SkeletonBookshelf() {
  return (
    <div className="mt-10">
      <div className="flex items-end gap-1">
        {Array.from({ length: 8 }).map((_, i) => (
          <div
            key={i}
            className="animate-pulse border border-hairline-strong bg-hairline"
            style={{ width: 22, height: 96 + (i * 7) % 24 }}
          />
        ))}
      </div>
      <div className="border-b border-dashed border-hairline-strong" />
    </div>
  );
}
