'use client';

import { useEffect, useState } from 'react';

const HANGUL_BASE = 0xac00;
const HANGUL_END = 0xd7a3;
const JUNG_COUNT = 21;
const JONG_COUNT = 28;

const CHO = [
  'ㄱ', 'ㄲ', 'ㄴ', 'ㄷ', 'ㄸ', 'ㄹ', 'ㅁ', 'ㅂ', 'ㅃ', 'ㅅ',
  'ㅆ', 'ㅇ', 'ㅈ', 'ㅉ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ',
];

function compose(cho: number, jung: number, jong = 0): string {
  return String.fromCharCode(HANGUL_BASE + cho * JUNG_COUNT * JONG_COUNT + jung * JONG_COUNT + jong);
}

// 한 음절을 타자 진행 단계 배열로 분해. 예: '늘' → ['ㄴ', '느', '늘']
function expandSyllable(syllable: string): string[] {
  const code = syllable.charCodeAt(0);
  if (code < HANGUL_BASE || code > HANGUL_END) return [syllable];
  const idx = code - HANGUL_BASE;
  const cho = Math.floor(idx / (JUNG_COUNT * JONG_COUNT));
  const jung = Math.floor((idx % (JUNG_COUNT * JONG_COUNT)) / JONG_COUNT);
  const jong = idx % JONG_COUNT;
  const steps = [CHO[cho], compose(cho, jung)];
  if (jong > 0) steps.push(compose(cho, jung, jong));
  return steps;
}

// 전체 텍스트를 타자 시퀀스로 펼침. 누적 결과 문자열 배열.
function buildSequence(text: string): string[] {
  const out: string[] = [];
  let prefix = '';
  for (const ch of text) {
    const expanded = expandSyllable(ch);
    for (let i = 0; i < expanded.length; i++) {
      out.push(prefix + expanded[i]);
    }
    prefix += expanded[expanded.length - 1];
  }
  return out;
}

type Props = {
  text: string;
  /** 자모 한 단계 사이 ms */
  speed?: number;
  /** 음절 사이 추가 멈춤 ms */
  syllablePause?: number;
  className?: string;
};

export function Typewriter({ text, speed = 110, syllablePause = 40, className }: Props) {
  const sequence = buildSequence(text);
  const [index, setIndex] = useState(0);
  const done = index >= sequence.length;

  useEffect(() => {
    if (done) return;
    const isEndOfSyllable = (() => {
      if (index + 1 >= sequence.length) return true;
      const cur = sequence[index];
      const next = sequence[index + 1];
      return next.length > cur.length && next.startsWith(cur);
    })();
    const delay = isEndOfSyllable ? speed + syllablePause : speed;
    const handle = setTimeout(() => setIndex((i) => i + 1), delay);
    return () => clearTimeout(handle);
  }, [index, sequence, speed, syllablePause, done]);

  const shown = done ? text : index === 0 ? '' : sequence[index - 1];

  return (
    <span className={className}>
      {shown}
      {done ? null : (
        <span className="caret" aria-hidden>
          |
        </span>
      )}
    </span>
  );
}
