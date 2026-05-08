package com.daner.comment.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.ALWAYS)
public record CommentSliceResponse(List<CommentResponse> comments, String nextCursor) {
}
