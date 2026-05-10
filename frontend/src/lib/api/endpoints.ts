// Endpoint 함수. 컴포넌트는 useQuery / useMutation으로 감싸서 사용.

import { apiFetch } from './client';
import type {
  AccessTokenResponse,
  AuthTokens,
  CommentSlice,
  Comment,
  HomeData,
  LikeState,
  NicknameCheck,
  NotificationSlice,
  Reply,
  ReplySlice,
  User,
  WordRoomResponse,
} from '@/types/api';

// ----- Auth -----

export const checkNickname = (nickname: string) =>
  apiFetch<NicknameCheck>(`/auth/check-nickname?nickname=${encodeURIComponent(nickname)}`, {
    attachAuth: false,
  });

export const signup = (signupToken: string, nickname: string) =>
  apiFetch<AuthTokens>('/auth/signup', {
    method: 'POST',
    body: JSON.stringify({ signupToken, nickname }),
    attachAuth: false,
  });

export const refreshAccessToken = (refreshToken: string) =>
  apiFetch<AccessTokenResponse>('/auth/refresh', {
    method: 'POST',
    body: JSON.stringify({ refreshToken }),
    attachAuth: false,
  });

export const logout = () =>
  apiFetch<void>('/auth/logout', { method: 'POST' });

// ----- Home -----

export const fetchHome = () => apiFetch<HomeData>('/home');

// ----- Word -----

export const fetchWordRoom = (word: string) =>
  apiFetch<WordRoomResponse>(`/words/${encodeURIComponent(word)}`);

export const fetchComments = (word: string, sort: 'latest' | 'popular', cursor?: string | null) => {
  const params = new URLSearchParams({ sort });
  if (cursor) params.set('cursor', cursor);
  return apiFetch<CommentSlice>(`/words/${encodeURIComponent(word)}/comments?${params.toString()}`);
};

export const createComment = (
  word: string,
  body: { content: string; anonymous?: boolean },
  asGuest: boolean,
) =>
  apiFetch<Comment>(`/words/${encodeURIComponent(word)}/comments`, {
    method: 'POST',
    body: JSON.stringify(body),
    attachAnonymous: asGuest,
  });

// ----- Comment -----

export const fetchReplies = (commentId: number, cursor?: string | null) => {
  const params = new URLSearchParams();
  if (cursor) params.set('cursor', cursor);
  const qs = params.toString();
  return apiFetch<ReplySlice>(`/comments/${commentId}/replies${qs ? `?${qs}` : ''}`);
};

export const createReply = (
  commentId: number,
  body: { content: string; anonymous?: boolean },
  asGuest: boolean,
) =>
  apiFetch<Reply>(`/comments/${commentId}/replies`, {
    method: 'POST',
    body: JSON.stringify(body),
    attachAnonymous: asGuest,
  });

export const deleteComment = (commentId: number) =>
  apiFetch<void>(`/comments/${commentId}`, { method: 'DELETE' });

// ----- Admin -----

export type WordWipeResponse = { word: string; removed: number };

export const wipeWordRoom = (word: string) =>
  apiFetch<WordWipeResponse>(`/words/${encodeURIComponent(word)}/comments`, { method: 'DELETE' });

// ----- Like -----

export const likeComment = (commentId: number) =>
  apiFetch<LikeState>(`/comments/${commentId}/like`, { method: 'POST' });

export const unlikeComment = (commentId: number) =>
  apiFetch<LikeState>(`/comments/${commentId}/like`, { method: 'DELETE' });

// ----- Profile / Notifications -----

export type MyProfile = {
  user: User;
  myWords: Array<{ id: number; word: string; myCommentCount: number; lastActivityAt: string }>;
  nextCursor: string | null;
};

export const fetchMyProfile = (cursor?: string | null) => {
  const params = new URLSearchParams();
  if (cursor) params.set('cursor', cursor);
  const qs = params.toString();
  return apiFetch<MyProfile>(`/users/me${qs ? `?${qs}` : ''}`);
};

export const fetchNotifications = (cursor?: string | null) => {
  const params = new URLSearchParams();
  if (cursor) params.set('cursor', cursor);
  const qs = params.toString();
  return apiFetch<NotificationSlice>(`/users/me/notifications${qs ? `?${qs}` : ''}`);
};

export const markNotificationsRead = (notificationIds: number[]) =>
  apiFetch<void>('/users/me/notifications/read', {
    method: 'PATCH',
    body: JSON.stringify({ notificationIds }),
  });
