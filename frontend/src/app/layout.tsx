import type { Metadata } from "next";
import { Geist, Geist_Mono } from "next/font/google";
import "./globals.css";
import { QueryProvider } from "@/lib/hooks/QueryProvider";
import { HelpFloatingButton } from "@/components/ui/HelpFloatingButton";

const geistSans = Geist({
  variable: "--font-geist-sans",
  subsets: ["latin"],
});

const geistMono = Geist_Mono({
  variable: "--font-geist-mono",
  subsets: ["latin"],
});

export const metadata: Metadata = {
  title: {
    default: "DANER",
    template: "%s | DANER",
  },
  description:
    "단어 하나가 하나의 방이 됩니다. 같은 단어를 떠올린 사람들이 한마디씩 남기는 곳.",
  openGraph: {
    title: "DANER",
    description: "단어로 모이는 작은 커뮤니티",
    siteName: "DANER",
    locale: "ko_KR",
    type: "website",
  },
  twitter: {
    card: "summary_large_image",
    title: "DANER",
    description: "단어로 모이는 작은 커뮤니티",
  },
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html
      lang="ko"
      className={`${geistSans.variable} ${geistMono.variable} h-full antialiased`}
    >
      <body className="min-h-full flex flex-col">
        <QueryProvider>
          {children}
          <HelpFloatingButton />
        </QueryProvider>
      </body>
    </html>
  );
}
