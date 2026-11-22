package com.demo.ranktracker.repository;

import com.demo.ranktracker.entity.Alert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {

    Page<Alert> findByKeywordWebsiteIdOrderByCreatedAtDesc(Long websiteId, Pageable pageable);

    List<Alert> findByKeywordWebsiteIdOrderByCreatedAtDesc(Long websiteId);

    long countByKeywordWebsiteIdAndReadFalse(Long websiteId);

    List<Alert> findByKeywordWebsiteIdAndReadFalseOrderByCreatedAtDesc(Long websiteId);
}
