package com.daner.comment.dto;

import java.util.List;

public record ReplySliceResponse(List<ReplyResponse> replies, String nextCursor) {
}
