import { ImageResponse } from 'next/og';
import { readFile } from 'node:fs/promises';
import path from 'node:path';

// 단어 페이지 동적 OG — path 의 단어를 그대로 큰 글씨로.
// 예: https://daner.kr/words/페르소나 미리보기에 "페르소나" 가 가운데에 크게.

export const alt = 'DANER — 단어 방';
export const size = { width: 1200, height: 630 };
export const contentType = 'image/png';

type Props = { params: Promise<{ word: string }> };

export default async function WordOgImage({ params }: Props) {
  const { word: encoded } = await params;
  const word = decodeURIComponent(encoded);

  const fontPath = path.join(process.cwd(), 'public/fonts/MemomentKkukkukk.ttf');
  const fontData = await readFile(fontPath);

  // 한글 5자 / 영어 10자 안팎이면 단일 폰트 크기. 길면 약간 줄임.
  const visualLen = [...word].reduce(
    (acc, ch) => acc + (/[\x20-\x7E]/.test(ch) ? 0.5 : 1),
    0,
  );
  const fontSize = visualLen <= 4 ? 220 : visualLen <= 7 ? 160 : 120;

  return new ImageResponse(
    (
      <div
        style={{
          width: '100%',
          height: '100%',
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          justifyContent: 'center',
          background: '#fdfcfa',
          fontFamily: 'Memoment',
        }}
      >
        <div
          style={{
            fontSize,
            color: '#1a1a1a',
            fontWeight: 700,
            letterSpacing: '-0.02em',
            padding: '0 80px',
            textAlign: 'center',
            maxWidth: '90%',
          }}
        >
          {word}
        </div>
        <div
          style={{
            marginTop: 64,
            fontSize: 26,
            color: '#9a9a9a',
            letterSpacing: '0.4em',
          }}
        >
          DANER
        </div>
      </div>
    ),
    {
      ...size,
      fonts: [
        {
          name: 'Memoment',
          data: fontData,
          style: 'normal',
          weight: 700,
        },
      ],
    },
  );
}
