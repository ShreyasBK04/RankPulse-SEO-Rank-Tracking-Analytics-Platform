package com.demo.ranktracker.controller;

import com.demo.ranktracker.dto.WebsiteCreateRequest;
import com.demo.ranktracker.dto.WebsiteDTO;
import com.demo.ranktracker.service.WebsiteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/websites")
public class WebsiteController {

    private final WebsiteService websiteService;

    public WebsiteController(WebsiteService websiteService) {
        this.websiteService = websiteService;
    }

    @GetMapping
    public ResponseEntity<List<WebsiteDTO>> getAllWebsites() {
        return ResponseEntity.ok(websiteService.getAllWebsites());
    }

    @GetMapping("/{id}")
    public ResponseEntity<WebsiteDTO> getWebsite(@PathVariable Long id) {
        return ResponseEntity.ok(websiteService.getWebsite(id));
    }

    @PostMapping
    public ResponseEntity<WebsiteDTO> createWebsite(@Valid @RequestBody WebsiteCreateRequest request) {
        WebsiteDTO created = websiteService.createWebsite(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<WebsiteDTO> updateWebsite(@PathVariable Long id,
                                                     @Valid @RequestBody WebsiteCreateRequest request) {
        return ResponseEntity.ok(websiteService.updateWebsite(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWebsite(@PathVariable Long id) {
        websiteService.deleteWebsite(id);
        return ResponseEntity.noContent().build();
    }
}
