package com.dztech.auth.controller;

import com.dztech.auth.dto.FaqRequest;
import com.dztech.auth.dto.FaqResponse;
import com.dztech.auth.service.FaqService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/faq")
@RequiredArgsConstructor
public class FaqController {

    private final FaqService faqService;

    @GetMapping
    public ResponseEntity<List<FaqResponse>> getAllFaqs(@RequestHeader("appId") String appId) {
        validateAppId(appId);
        List<FaqResponse> faqs = faqService.getFaqsByAppId(appId);
        return ResponseEntity.ok(faqs);
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<FaqResponse>> getFaqsByCategory(
            @RequestHeader("appId") String appId,
            @PathVariable String category) {
        validateAppId(appId);
        List<FaqResponse> faqs = faqService.getFaqsByAppIdAndCategory(appId, category);
        return ResponseEntity.ok(faqs);
    }

    @GetMapping("/category/{category}/subcategory/{subCategory}")
    public ResponseEntity<List<FaqResponse>> getFaqsByCategoryAndSubCategory(
            @RequestHeader("appId") String appId,
            @PathVariable String category,
            @PathVariable String subCategory) {
        validateAppId(appId);
        List<FaqResponse> faqs = faqService.getFaqsByAppIdAndCategoryAndSubCategory(appId, category, subCategory);
        return ResponseEntity.ok(faqs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FaqResponse> getFaqById(@PathVariable Long id) {
        FaqResponse faq = faqService.getFaqById(id);
        return ResponseEntity.ok(faq);
    }

    @PostMapping
    public ResponseEntity<FaqResponse> createFaq(@Valid @RequestBody FaqRequest request) {
        FaqResponse faq = faqService.createFaq(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(faq);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FaqResponse> updateFaq(
            @PathVariable Long id,
            @Valid @RequestBody FaqRequest request) {
        FaqResponse faq = faqService.updateFaq(id, request);
        return ResponseEntity.ok(faq);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFaq(@PathVariable Long id) {
        faqService.deleteFaq(id);
        return ResponseEntity.noContent().build();
    }

    private void validateAppId(String appId) {
        if (!"rydd".equals(appId) && !"rydc".equals(appId)) {
            throw new IllegalArgumentException("Invalid app ID. Must be 'rydd' or 'rydc'");
        }
    }
}
