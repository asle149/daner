import { ImageResponse } from 'next/og';
import { readFile } from 'node:fs/promises';
import path from 'node:path';

// Next.js 16 app router 의 동적 아이콘 — Memoment Kkukkuk 폰트로 'D' 한 글자.
// favicon.ico 보다 우선 적용됨.

export const size = { width: 64, height: 64 };
export const contentType = 'image/png';

export default async function Icon() {
  const fontPath = path.join(process.cwd(), 'public/fonts/MemomentKkukkukk.ttf');
  const fontData = await readFile(fontPath);

  return new ImageResponse(
    (
      <div
        style={{
          width: '100%',
          height: '100%',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          background: '#fdfcfa',
          color: '#1a1a1a',
          fontFamily: 'Memoment',
          fontSize: 56,
          fontWeight: 700,
          letterSpacing: '-0.02em',
        }}
      >
        D
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
