package com.daner.word.service;

import com.daner.comment.repository.CommentRepository;
import com.daner.word.entity.PopularWordDaily;
import com.daner.word.repository.PopularWordDailyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PopularWordBatchService {

    private static final int TOP_N = 3;
    private static final int WINDOW_HOURS = 24;

    private final CommentRepository commentRepository;
    private final PopularWordDailyRepository popularWordDailyRepository;

    @Scheduled(cron = "${app.popular-words.cron:0 0 * * * *}")
    @Transactional
    public void recalculate() {
        LocalDateTime since = LocalDateTime.now().minusHours(WINDOW_HOURS);
        List<CommentRepository.WordCommentCount> top = commentRepository
                .findTopWordsByCommentCountSince(since, PageRequest.of(0, TOP_N));
        popularWordDailyRepository.deleteAllInBatch();
        int rank = 1;
        for (CommentRepository.WordCommentCount entry : top) {
            popularWordDailyRepository.save(new PopularWordDaily(
                    entry.getWordId(), entry.getCnt().intValue(), rank++));
        }
        log.info("popular words recalculated: {} entries since {}", top.size(), since);
    }
}
