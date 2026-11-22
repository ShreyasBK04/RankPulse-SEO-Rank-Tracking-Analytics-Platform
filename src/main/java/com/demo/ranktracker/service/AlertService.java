package com.demo.ranktracker.service;

import com.demo.ranktracker.dto.AlertDTO;
import com.demo.ranktracker.entity.Alert;
import com.demo.ranktracker.exception.ResourceNotFoundException;
import com.demo.ranktracker.repository.AlertRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class AlertService {

    private final AlertRepository alertRepository;

    public AlertService(AlertRepository alertRepository) {
        this.alertRepository = alertRepository;
    }

    public Page<AlertDTO> getAlertsByWebsite(Long websiteId, Pageable pageable) {
        return alertRepository.findByKeywordWebsiteIdOrderByCreatedAtDesc(websiteId, pageable)
                .map(this::toDTO);
    }

    public long getUnreadCount(Long websiteId) {
        return alertRepository.countByKeywordWebsiteIdAndReadFalse(websiteId);
    }

    @Transactional
    public AlertDTO markAsRead(Long alertId) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("Alert", alertId));
        alert.setRead(true);
        return toDTO(alertRepository.save(alert));
    }

    @Transactional
    public void markAllAsRead(Long websiteId) {
        List<Alert> unread = alertRepository
                .findByKeywordWebsiteIdAndReadFalseOrderByCreatedAtDesc(websiteId);
        unread.forEach(alert -> alert.setRead(true));
        alertRepository.saveAll(unread);
    }

    private AlertDTO toDTO(Alert alert) {
        return new AlertDTO(
                alert.getId(),
                alert.getKeyword().getId(),
                alert.getKeyword().getKeyword(),
                alert.getKeyword().getWebsite().getDomain(),
                alert.getAlertType(),
                alert.getMessage(),
                alert.getOldPosition(),
                alert.getNewPosition(),
                alert.isRead(),
                alert.getCreatedAt()
        );
    }
}
