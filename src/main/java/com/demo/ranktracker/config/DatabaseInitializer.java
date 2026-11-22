package com.demo.ranktracker.config;

import com.demo.ranktracker.entity.*;
import com.demo.ranktracker.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * Initializes the database with sample websites, keywords, and historical rankings
 * if the database is currently empty. Ensures charts and tables display realistic data immediately.
 */
@Component
public class DatabaseInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseInitializer.class);

    private final WebsiteRepository websiteRepository;
    private final KeywordRepository keywordRepository;
    private final RankingRepository rankingRepository;
    private final AlertRepository alertRepository;

    public DatabaseInitializer(WebsiteRepository websiteRepository,
                               KeywordRepository keywordRepository,
                               RankingRepository rankingRepository,
                               AlertRepository alertRepository) {
        this.websiteRepository = websiteRepository;
        this.keywordRepository = keywordRepository;
        this.rankingRepository = rankingRepository;
        this.alertRepository = alertRepository;
    }

    @Override
    public void run(String... args) {
        if (websiteRepository.count() > 0) {
            log.info("Database already contains data ({} websites found). Skipping seed initialization.",
                    websiteRepository.count());
            return;
        }

        log.info("Empty database detected. Seeding initial demonstration data...");

        // 1. Websites
        Website embarkx = new Website("embarkx.com", "EmbarkX Official");
        Website spring = new Website("spring.io", "Spring Official");
        websiteRepository.saveAll(List.of(embarkx, spring));

        // 2. Keywords
        Keyword kw1 = new Keyword(embarkx, "spring boot tutorial");
        Keyword kw2 = new Keyword(embarkx, "java microservices course");
        Keyword kw3 = new Keyword(embarkx, "learn spring framework");
        Keyword kw4 = new Keyword(embarkx, "spring security guide");
        Keyword kw5 = new Keyword(embarkx, "spring boot interview questions");
        keywordRepository.saveAll(List.of(kw1, kw2, kw3, kw4, kw5));

        // 3. Historical rankings for kw1 (16 -> 15 -> 13 -> 11 -> 10 -> 8 -> 6)
        LocalDate today = LocalDate.now();
        int[] positionsKw1 = {16, 15, 13, 11, 10, 8, 6};
        for (int i = 0; i < positionsKw1.length; i++) {
            Ranking r = new Ranking();
            r.setKeyword(kw1);
            r.setPosition(positionsKw1[i]);
            r.setSearchUrl("https://embarkx.com/courses/spring-boot");
            r.setCheckedOn(today.minusDays(6 - i));
            rankingRepository.save(r);
        }

        // Rankings for kw2 (9 -> 9 -> 10 -> 12 -> 14 -> 15 -> 16)
        int[] positionsKw2 = {9, 9, 10, 12, 14, 15, 16};
        for (int i = 0; i < positionsKw2.length; i++) {
            Ranking r = new Ranking();
            r.setKeyword(kw2);
            r.setPosition(positionsKw2[i]);
            r.setSearchUrl("https://embarkx.com/courses/microservices");
            r.setCheckedOn(today.minusDays(6 - i));
            rankingRepository.save(r);
        }

        // Rankings for kw3 (4 -> 4 -> 3 -> 3 -> 3 -> 2 -> 2)
        int[] positionsKw3 = {4, 4, 3, 3, 3, 2, 2};
        for (int i = 0; i < positionsKw3.length; i++) {
            Ranking r = new Ranking();
            r.setKeyword(kw3);
            r.setPosition(positionsKw3[i]);
            r.setSearchUrl("https://embarkx.com/tutorials/spring");
            r.setCheckedOn(today.minusDays(6 - i));
            rankingRepository.save(r);
        }

        // Rankings for kw4 (45 -> 60 -> 85 -> null)
        Integer[] positionsKw4 = {45, 60, 85, null};
        for (int i = 0; i < positionsKw4.length; i++) {
            Ranking r = new Ranking();
            r.setKeyword(kw4);
            r.setPosition(positionsKw4[i]);
            r.setSearchUrl(positionsKw4[i] != null ? "https://embarkx.com/guides/security" : null);
            r.setCheckedOn(today.minusDays(3 - i));
            rankingRepository.save(r);
        }

        // Rankings for kw5 (25 -> 24 -> 22 -> 20 -> 18)
        int[] positionsKw5 = {25, 24, 22, 20, 18};
        for (int i = 0; i < positionsKw5.length; i++) {
            Ranking r = new Ranking();
            r.setKeyword(kw5);
            r.setPosition(positionsKw5[i]);
            r.setSearchUrl("https://embarkx.com/interview/spring-boot");
            r.setCheckedOn(today.minusDays(4 - i));
            rankingRepository.save(r);
        }

        // 4. Initial alerts
        Alert a1 = new Alert(kw1, AlertType.ENTERED_TOP_10,
                "'spring boot tutorial' entered the top 10 (#11 → #10)", 11, 10);
        Alert a2 = new Alert(kw1, AlertType.SIGNIFICANT_IMPROVEMENT,
                "'spring boot tutorial' improved 5 positions (#13 → #8)", 13, 8);
        Alert a3 = new Alert(kw2, AlertType.SIGNIFICANT_DECLINE,
                "'java microservices course' declined 6 positions (#9 → #15)", 9, 15);
        Alert a4 = new Alert(kw4, AlertType.DROPPED_FROM_TOP_100,
                "'spring security guide' dropped out of the top 100 (was #85)", 85, null);
        alertRepository.saveAll(List.of(a1, a2, a3, a4));

        log.info("Demo data initialization complete: 2 websites, 5 keywords, 29 ranking points, 4 alerts created.");
    }
}
