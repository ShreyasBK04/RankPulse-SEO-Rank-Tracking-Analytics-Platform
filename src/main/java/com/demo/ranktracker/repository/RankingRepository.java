package com.demo.ranktracker.repository;

import com.demo.ranktracker.entity.Ranking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface RankingRepository extends JpaRepository<Ranking, Long> {

    boolean existsByKeywordIdAndCheckedOn(Long keywordId, LocalDate checkedOn);

    Optional<Ranking> findByKeywordIdAndCheckedOn(Long keywordId, LocalDate checkedOn);

    List<Ranking> findByKeywordIdOrderByCheckedOnDesc(Long keywordId);

    List<Ranking> findByKeywordIdAndCheckedOnBetweenOrderByCheckedOn(
            Long keywordId, LocalDate start, LocalDate end);

    List<Ranking> findTop2ByKeywordIdOrderByCheckedOnDesc(Long keywordId);

    Optional<Ranking> findTopByKeywordIdOrderByCheckedOnDesc(Long keywordId);

    @Query("SELECT MIN(r.position) FROM Ranking r " +
           "WHERE r.keyword.id = :keywordId AND r.position IS NOT NULL")
    Optional<Integer> findBestPosition(@Param("keywordId") Long keywordId);

    @Query("SELECT r FROM Ranking r WHERE r.keyword.website.id = :websiteId " +
           "AND r.checkedOn BETWEEN :start AND :end ORDER BY r.checkedOn")
    List<Ranking> findByWebsiteIdAndDateRange(
            @Param("websiteId") Long websiteId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end);

    @Query("SELECT r FROM Ranking r WHERE r.keyword.website.id = :websiteId " +
           "ORDER BY r.checkedOn DESC")
    List<Ranking> findAllByWebsiteIdOrderByCheckedOnDesc(@Param("websiteId") Long websiteId);

    @Query("SELECT DISTINCT r.checkedOn FROM Ranking r " +
           "WHERE r.keyword.website.id = :websiteId ORDER BY r.checkedOn")
    List<LocalDate> findDistinctDatesByWebsiteId(@Param("websiteId") Long websiteId);
}
