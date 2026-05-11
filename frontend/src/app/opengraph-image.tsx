import { ImageResponse } from 'next/og';
import { readFile } from 'node:fs/promises';
import path from 'node:path';

// Next.js 16 의 동적 OG 이미지 — 카카오/슬랙/디스코드 링크 미리보기에 사용됨.
// 사이트 본문과 같은 톤 (배경 #fdfcfa, 본문 회색)으로 "오늘의 단어는?" 한 줄.

export const alt = 'DANER — 단어로 모이는 작은 커뮤니티';
export const size = { width: 1200, height: 630 };
export const contentType = 'image/png';

export default async function OgImage() {
  const fontPath = path.join(process.cwd(), 'public/fonts/MemomentKkukkukk.ttf');
  const fontData = await readFile(fontPath);

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
            fontSize: 92,
            color: '#5a5a5a',
            fontWeight: 700,
            letterSpacing: '-0.01em',
          }}
        >
          오늘의 단어는?
        </div>
        <div
          style={{
            marginTop: 56,
            fontSize: 28,
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
