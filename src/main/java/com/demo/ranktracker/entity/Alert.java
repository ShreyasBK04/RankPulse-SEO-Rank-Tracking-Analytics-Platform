package com.demo.ranktracker.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "alerts", indexes = {
    @Index(name = "idx_alert_keyword", columnList = "keyword_id"),
    @Index(name = "idx_alert_read", columnList = "is_read")
})
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "keyword_id", nullable = false)
    private Keyword keyword;

    @Enumerated(EnumType.STRING)
    @Column(name = "alert_type", nullable = false, length = 50)
    private AlertType alertType;

    @Column(nullable = false, length = 500)
    private String message;

    @Column(name = "old_position")
    private Integer oldPosition;

    @Column(name = "new_position")
    private Integer newPosition;

    @Column(name = "is_read", nullable = false)
    private boolean read = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Alert() {}

    public Alert(Keyword keyword, AlertType alertType, String message,
                 Integer oldPosition, Integer newPosition) {
        this.keyword = keyword;
        this.alertType = alertType;
        this.message = message;
        this.oldPosition = oldPosition;
        this.newPosition = newPosition;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Keyword getKeyword() { return keyword; }
    public void setKeyword(Keyword keyword) { this.keyword = keyword; }

    public AlertType getAlertType() { return alertType; }
    public void setAlertType(AlertType alertType) { this.alertType = alertType; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Integer getOldPosition() { return oldPosition; }
    public void setOldPosition(Integer oldPosition) { this.oldPosition = oldPosition; }

    public Integer getNewPosition() { return newPosition; }
    public void setNewPosition(Integer newPosition) { this.newPosition = newPosition; }

    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}
