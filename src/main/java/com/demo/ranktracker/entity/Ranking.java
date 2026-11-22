package com.demo.ranktracker.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "rankings",
       uniqueConstraints = @UniqueConstraint(
           name = "uk_keyword_date",
           columnNames = {"keyword_id", "checked_on"}))
public class Ranking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "keyword_id", nullable = false)
    private Keyword keyword;

    @Column
    private Integer position; // 1–100, or null when not in top 100

    @Column(name = "search_url", length = 2048)
    private String searchUrl;

    @Column(name = "checked_on", nullable = false)
    private LocalDate checkedOn;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Ranking() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Keyword getKeyword() { return keyword; }
    public void setKeyword(Keyword keyword) { this.keyword = keyword; }

    public Integer getPosition() { return position; }
    public void setPosition(Integer position) { this.position = position; }

    public String getSearchUrl() { return searchUrl; }
    public void setSearchUrl(String searchUrl) { this.searchUrl = searchUrl; }

    public LocalDate getCheckedOn() { return checkedOn; }
    public void setCheckedOn(LocalDate checkedOn) { this.checkedOn = checkedOn; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}
