package com.demo.ranktracker.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "keywords",
       uniqueConstraints = @UniqueConstraint(
           name = "uk_website_keyword",
           columnNames = {"website_id", "keyword"}))
public class Keyword {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "website_id", nullable = false)
    private Website website;

    @Column(nullable = false, length = 255)
    private String keyword;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "keyword", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Ranking> rankings = new ArrayList<>();

    @OneToMany(mappedBy = "keyword", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Alert> alerts = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Keyword() {}

    public Keyword(Website website, String keyword) {
        this.website = website;
        this.keyword = keyword;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Website getWebsite() { return website; }
    public void setWebsite(Website website) { this.website = website; }

    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public List<Ranking> getRankings() { return rankings; }
    public List<Alert> getAlerts() { return alerts; }
}
