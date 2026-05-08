package com.daner.word.repository;

import com.daner.word.entity.PopularWordDaily;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PopularWordDailyRepository extends JpaRepository<PopularWordDaily, Long> {

    List<PopularWordDaily> findAllByOrderByRankPositionAsc();

    @Query("SELECT p.wordId AS id, w.word AS word, p.commentCount AS commentCount " +
            "FROM PopularWordDaily p, com.daner.word.entity.Word w " +
            "WHERE w.id = p.wordId ORDER BY p.rankPosition ASC")
    List<PopularWordView> findPopularWordsOrdered();

    interface PopularWordView {
        Long getId();

        String getWord();

        int getCommentCount();
    }
}
