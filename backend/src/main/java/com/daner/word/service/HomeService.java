package com.daner.word.service;

import com.daner.comment.repository.CommentRepository;
import com.daner.word.dto.HomeResponse;
import com.daner.word.entity.PopularWordDaily;
import com.daner.word.repository.PopularWordDailyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HomeService {

    private final CommentRepository commentRepository;
    private final PopularWordDailyRepository popularWordDailyRepository;

    @Transactional(readOnly = true)
    public HomeResponse getHome(Long currentUserId) {
        List<HomeResponse.MyWord> myWords = currentUserId == null
                ? List.of()
                : commentRepository.findMyWords(currentUserId).stream()
                        .map(p -> new HomeResponse.MyWord(
                                p.getId(), p.getWord(), p.getCommentCount(),
                                p.getLastActivityAt(), p.getMyLastCommentAt()))
                        .toList();

        List<HomeResponse.PopularWord> popular = popularWordDailyRepository.findPopularWordsOrdered().stream()
                .map(v -> new HomeResponse.PopularWord(v.getId(), v.getWord(), v.getCommentCount()))
                .toList();

        return new HomeResponse(myWords, popular);
    }
}
