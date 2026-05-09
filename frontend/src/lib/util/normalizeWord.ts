// 클라이언트 측 단어 정규화 (백엔드와 동일 규칙).
// 라우팅 전에 trim/소문자/NFC + 공백 제거. 길이는 backend가 최종 판단.
// 공백 포함 입력은 거부 대신 silently 제거 — 사용자가 "공중 화장실" 같이 쳐도
// /words/공중화장실 으로 이동시켜 "없는 단어" 빈 상태를 보여줌.

export function normalizeForRouting(input: string): string {
  const stripped = input.replace(/\s+/g, '');
  if (!stripped) return '';
  return stripped.normalize('NFC').toLowerCase();
}
