// 클라이언트 측 단어 정규화 (백엔드와 동일 규칙).
// 라우팅 전 trim/소문자/NFC만 적용. 길이/공백 검증은 backend가 최종 결정.

const WHITESPACE = /\s/;

export function normalizeForRouting(input: string): string {
  const trimmed = input.trim();
  if (!trimmed) return '';
  if (WHITESPACE.test(trimmed)) {
    throw new Error('단어에 띄어쓰기를 포함할 수 없어요.');
  }
  return trimmed.normalize('NFC').toLowerCase();
}
