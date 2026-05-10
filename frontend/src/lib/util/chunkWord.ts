// 단어를 줄바꿈용으로 자른다. 한글은 5자, 영어/숫자는 10자가 한 줄.
// 시각 폭은 한글 1, ASCII 0.5로 잡고 한 줄당 시각 폭 5를 넘지 않게.
const LINE_VISUAL = 5;

function isAsciiNarrow(ch: string): boolean {
  return /[\x20-\x7E]/.test(ch);
}

function visualWidth(ch: string): number {
  return isAsciiNarrow(ch) ? 0.5 : 1;
}

export function chunkWord(word: string): string[] {
  const chunks: string[] = [];
  let cur = '';
  let curW = 0;
  for (const ch of word) {
    const w = visualWidth(ch);
    if (curW + w > LINE_VISUAL && cur.length > 0) {
      chunks.push(cur);
      cur = '';
      curW = 0;
    }
    cur += ch;
    curW += w;
  }
  if (cur) chunks.push(cur);
  return chunks;
}
