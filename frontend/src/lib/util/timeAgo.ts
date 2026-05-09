// 백엔드의 LocalDateTime은 timezone 없는 ISO 문자열로 직렬화됨.
// 그대로 new Date()로 파싱하면 로컬 타임존(=KST)으로 잘못 해석되므로
// timezone 표기가 없으면 'Z'를 붙여 UTC로 강제 파싱한다.
function parseUtc(iso: string): Date {
  const hasTz = /(Z|[+-]\d{2}:?\d{2})$/.test(iso);
  return new Date(hasTz ? iso : iso + 'Z');
}

function pad2(n: number): string {
  return n < 10 ? `0${n}` : String(n);
}

export function timeAgo(iso: string): string {
  const date = parseUtc(iso);
  const now = new Date();
  const sameYear = date.getFullYear() === now.getFullYear();
  const m = date.getMonth() + 1;
  const d = date.getDate();
  const hh = pad2(date.getHours());
  const mm = pad2(date.getMinutes());
  if (sameYear) return `${m}/${d} ${hh}:${mm}`;
  return `${date.getFullYear()}.${m}/${d} ${hh}:${mm}`;
}
