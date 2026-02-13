package com.dztech.auth.controller;

import com.dztech.auth.dto.HelpCategoryRequest;
import com.dztech.auth.dto.HelpCategoryResponse;
import com.dztech.auth.dto.HelpItemRequest;
import com.dztech.auth.dto.HelpItemResponse;
import com.dztech.auth.service.HelpCategoryServiceSimple;
import com.dztech.auth.service.HelpItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/help")
@RequiredArgsConstructor
public class HelpController {

    private final HelpCategoryServiceSimple helpCategoryService;
    private final HelpItemService helpItemService;

    // Public endpoints - no authentication required
    @GetMapping
    public ResponseEntity<List<HelpCategoryResponse>> getAllHelpCategories(@RequestHeader("appId") String appId) {
        validateAppId(appId);
        List<HelpCategoryResponse> categories = helpCategoryService.getAllHelpCategories(appId);
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/{categoryKey}")
    public ResponseEntity<HelpCategoryResponse> getHelpCategoryByCategoryKey(
            @RequestHeader("appId") String appId,
            @PathVariable String categoryKey) {
        validateAppId(appId);
        HelpCategoryResponse category = helpCategoryService.getHelpCategoryByCategoryKey(appId, categoryKey);
        return ResponseEntity.ok(category);
    }

    @GetMapping("/categories/{id}")
    public ResponseEntity<HelpCategoryResponse> getHelpCategoryById(@PathVariable Long id) {
        HelpCategoryResponse category = helpCategoryService.getHelpCategoryById(id);
        return ResponseEntity.ok(category);
    }

    @GetMapping("/items/{id}")
    public ResponseEntity<HelpItemResponse> getHelpItemById(@PathVariable Long id) {
        HelpItemResponse item = helpItemService.getHelpItemById(id);
        return ResponseEntity.ok(item);
    }

    // Admin-only endpoints - require admin role
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<HelpCategoryResponse> createHelpCategory(@Valid @RequestBody HelpCategoryRequest request) {
        HelpCategoryResponse category = helpCategoryService.createHelpCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(category);
    }

    @PutMapping("/categories/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<HelpCategoryResponse> updateHelpCategory(
            @PathVariable Long id,
            @Valid @RequestBody HelpCategoryRequest request) {
        HelpCategoryResponse category = helpCategoryService.updateHelpCategory(id, request);
        return ResponseEntity.ok(category);
    }

    @DeleteMapping("/categories/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteHelpCategory(@PathVariable Long id) {
        helpCategoryService.deleteHelpCategory(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/items")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<HelpItemResponse> createHelpItem(@Valid @RequestBody HelpItemRequest request) {
        HelpItemResponse item = helpItemService.createHelpItem(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(item);
    }

    @PutMapping("/items/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<HelpItemResponse> updateHelpItem(
            @PathVariable Long id,
            @Valid @RequestBody HelpItemRequest request) {
        HelpItemResponse item = helpItemService.updateHelpItem(id, request);
        return ResponseEntity.ok(item);
    }

    @DeleteMapping("/items/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteHelpItem(@PathVariable Long id) {
        helpItemService.deleteHelpItem(id);
        return ResponseEntity.noContent().build();
    }

    private void validateAppId(String appId) {
        if (!"rydd".equals(appId) && !"rydc".equals(appId)) {
            throw new IllegalArgumentException("Invalid app ID. Must be 'rydd' or 'rydc'");
        }
    }
}