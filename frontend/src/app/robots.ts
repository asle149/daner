import type { MetadataRoute } from 'next';

export default function robots(): MetadataRoute.Robots {
  return {
    rules: [
      {
        userAgent: '*',
        // 단어 방은 검색 엔진이 인덱싱하지 않도록. 홈만 노출.
        allow: ['/'],
        disallow: ['/words/', '/me/', '/auth/'],
      },
    ],
  };
}
