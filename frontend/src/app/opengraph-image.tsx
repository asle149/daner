import { ImageResponse } from 'next/og';
import { readFile } from 'node:fs/promises';
import path from 'node:path';

// 메인 OG — 사이트의 background.png 살짝 깔고 "오늘의 단어는?" 한 줄.
// 카카오/슬랙/디스코드 등 링크 미리보기에 사용됨.

export const alt = 'DANER — 단어로 모이는 작은 커뮤니티';
export const size = { width: 1200, height: 630 };
export const contentType = 'image/png';

export default async function OgImage() {
  const fontPath = path.join(process.cwd(), 'public/fonts/MemomentKkukkukk.ttf');
  const bgPath = path.join(process.cwd(), 'public/background.png');
  const [fontData, bgData] = await Promise.all([readFile(fontPath), readFile(bgPath)]);
  const bgDataUri = `data:image/png;base64,${bgData.toString('base64')}`;

  return new ImageResponse(
    (
      <div
        style={{
          position: 'relative',
          width: '100%',
          height: '100%',
          display: 'flex',
          fontFamily: 'Memoment',
        }}
      >
        {/* 배경 PNG — 살짝 흐리게 깔아 본문 톤과 맞춤 */}
        <img
          src={bgDataUri}
          alt=""
          width={1200}
          height={630}
          style={{
            position: 'absolute',
            inset: 0,
            width: '100%',
            height: '100%',
            objectFit: 'cover',
            opacity: 0.7,
          }}
        />
        {/* 위에 옅은 베이지 톤 오버레이로 글자 가독성 확보 */}
        <div
          style={{
            position: 'absolute',
            inset: 0,
            background: 'rgba(253, 252, 250, 0.55)',
          }}
        />
        {/* 본문 */}
        <div
          style={{
            position: 'relative',
            width: '100%',
            height: '100%',
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            justifyContent: 'center',
          }}
        >
          <div
            style={{
              fontSize: 96,
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
