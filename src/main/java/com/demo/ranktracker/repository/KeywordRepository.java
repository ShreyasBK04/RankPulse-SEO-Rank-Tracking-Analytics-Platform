package com.demo.ranktracker.repository;

import com.demo.ranktracker.entity.Keyword;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KeywordRepository extends JpaRepository<Keyword, Long> {

    List<Keyword> findByWebsiteId(Long websiteId);

    Page<Keyword> findByWebsiteId(Long websiteId, Pageable pageable);

    boolean existsByWebsiteIdAndKeyword(Long websiteId, String keyword);

    Optional<Keyword> findByWebsiteIdAndKeyword(Long websiteId, String keyword);

    Page<Keyword> findByWebsiteIdAndKeywordContainingIgnoreCase(
            Long websiteId, String search, Pageable pageable);

    long countByWebsiteId(Long websiteId);
}
