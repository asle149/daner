// API 응답 표준 (백엔드 ApiResponse<T> 매칭)

export type ApiSuccess<T> = {
  success: true;
  data: T;
  error: null;
};

export type ApiFailure = {
  success: false;
  data: null;
  error: { code: string; message: string };
};

export type ApiResponse<T> = ApiSuccess<T> | ApiFailure;

// --- 도메인 타입 (api-spec.yaml 매칭) ---

export type User = {
  id: number;
  nickname: string;
  profileImageUrl: string | null;
};

export type WordRoom = {
  id: number;
  word: string;
  commentCount: number;
  likeCount: number;
  createdAt: string;
  exists: true;
};

export type EmptyWordRoom = {
  word: string;
  exists: false;
  message: string;
};

export type WordRoomResponse = WordRoom | EmptyWordRoom;

export type UserAuthor = { type: 'user'; id: number; nickname: string };
export type AnonymousAuthor = { type: 'anonymous'; label: string };
export type Author = UserAuthor | AnonymousAuthor;

export type Comment = {
  id: number;
  content: string;
  author: Author;
  likeCount: number;
  isLiked: boolean;
  isMine: boolean;
  replyCount: number;
  createdAt: string;
};

export type Reply = {
  id: number;
  parentId: number;
  content: string;
  author: Author;
  likeCount: number;
  isLiked: boolean;
  isMine: boolean;
  createdAt: string;
};

export type Slice<T, K extends string> = {
  nextCursor: string | null;
} & { [P in K]: T[] };

export type CommentSlice = Slice<Comment, 'comments'>;
export type ReplySlice = Slice<Reply, 'replies'>;

export type AuthTokens = {
  user: User;
  accessToken: string;
  refreshToken: string;
};

export type AccessTokenResponse = { accessToken: string };

export type NicknameCheck = { available: boolean; reason: string | null };

export type HomeData = {
  myWords: Array<{
    id: number;
    word: string;
    commentCount: number;
    lastActivityAt: string;
    myLastCommentAt: string;
  }>;
  popularWords: Array<{
    id: number;
    word: string;
    commentCount: number;
  }>;
};

export type Notification = {
  id: number;
  type: 'reply' | 'like';
  word: string;
  commentId: number;
  parentCommentId: number | null;
  actor: { nickname?: string; label?: string };
  preview: string | null;
  commentPreview: string | null;
  isRead: boolean;
  createdAt: string;
};

export type NotificationSlice = Slice<Notification, 'notifications'>;

export type LikeState = { likeCount: number; isLiked: boolean };
