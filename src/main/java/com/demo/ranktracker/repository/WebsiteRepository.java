package com.demo.ranktracker.repository;

import com.demo.ranktracker.entity.Website;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WebsiteRepository extends JpaRepository<Website, Long> {

    Optional<Website> findByDomain(String domain);

    boolean existsByDomain(String domain);
}
