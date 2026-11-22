package com.demo.ranktracker.controller;

import com.demo.ranktracker.dto.AlertDTO;
import com.demo.ranktracker.service.AlertService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    @GetMapping("/website/{websiteId}")
    public ResponseEntity<Page<AlertDTO>> getAlerts(
            @PathVariable Long websiteId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(alertService.getAlertsByWebsite(websiteId, pageRequest));
    }

    @GetMapping("/website/{websiteId}/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(@PathVariable Long websiteId) {
        return ResponseEntity.ok(Map.of("count", alertService.getUnreadCount(websiteId)));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<AlertDTO> markAsRead(@PathVariable Long id) {
        return ResponseEntity.ok(alertService.markAsRead(id));
    }

    @PutMapping("/website/{websiteId}/read-all")
    public ResponseEntity<Map<String, String>> markAllAsRead(@PathVariable Long websiteId) {
        alertService.markAllAsRead(websiteId);
        return ResponseEntity.ok(Map.of("message", "All alerts marked as read"));
    }
}
